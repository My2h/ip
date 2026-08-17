#!/usr/bin/env python3
"""Run the console UI test plan in test/ui-test-plan.md against the compiled
program, one command at a time, and stop at the first mismatch.

Usage (from the repository root):
    py .claude/skills/test-ui/scripts/run-ui-tests.py [path/to/ui-test-plan.md]
"""
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

DIVIDER_RE = re.compile(r"^ {4}_+\s*$")
CASE_RE = re.compile(r"^## Test Case: (.+)$", re.MULTILINE)
FIELD_RE = re.compile(
    r"\*\*Aim:\*\*\s*(?P<aim>.*?)\s*\n"
    r"\*\*Input:\*\*\s*```\n(?P<input>.*?)```\s*\n"
    r"\*\*Expected Output:\*\*\s*```\n(?P<expected>.*?)```",
    re.DOTALL,
)


class TestCase:
    def __init__(self, name, aim, input_lines, expected_lines):
        self.name = name
        self.aim = aim
        self.input_lines = input_lines
        self.expected_lines = expected_lines


def parse_plan(plan_path):
    text = plan_path.read_text(encoding="utf-8")
    headers = list(CASE_RE.finditer(text))
    cases = []
    for i, header in enumerate(headers):
        start = header.end()
        end = headers[i + 1].start() if i + 1 < len(headers) else len(text)
        body = text[start:end]
        match = FIELD_RE.search(body)
        if not match:
            raise ValueError(
                f"Test case '{header.group(1)}' is missing an Aim, Input, "
                "or Expected Output block."
            )
        input_lines = [line for line in match.group("input").split("\n") if line != ""]
        if len(input_lines) > 1:
            raise ValueError(
                f"Test case '{header.group(1)}' has {len(input_lines)} input "
                "lines. Each test case may send at most one command; split "
                "extra commands into their own test cases."
            )
        expected_lines = match.group("expected").rstrip("\n").split("\n")
        cases.append(TestCase(header.group(1), match.group("aim").strip(), input_lines, expected_lines))
    if not cases:
        raise ValueError(f"No '## Test Case: ...' sections found in {plan_path}")
    if cases[0].input_lines:
        raise ValueError("The first test case must be the startup case, with an empty Input block.")
    return cases


def find_main_class(src_dir):
    for java_file in sorted(src_dir.glob("*.java")):
        if re.search(r"public\s+static\s+void\s+main\s*\(", java_file.read_text(encoding="utf-8")):
            return java_file.stem
    raise ValueError(f"No file with a public static void main(...) found under {src_dir}")


def compile_program(src_dir, build_dir):
    java_files = [str(p) for p in src_dir.glob("*.java")]
    result = subprocess.run(
        ["javac", "-d", str(build_dir), *java_files],
        capture_output=True, text=True,
    )
    if result.returncode != 0:
        print("********** BUILD FAILURE **********")
        print(result.stderr)
        sys.exit(1)


def read_block(proc):
    """Read lines until a divider line, or EOF. Returns (content_lines, divider_line_or_None)."""
    content = []
    while True:
        line = proc.stdout.readline()
        if line == "":
            return content, None
        line = line.rstrip("\r\n")
        if DIVIDER_RE.match(line):
            return content, line
        content.append(line)


def strip_blank_edges(lines):
    start, end = 0, len(lines)
    while start < end and lines[start] == "":
        start += 1
    while end > start and lines[end - 1] == "":
        end -= 1
    return lines[start:end]


def describe_crash(proc):
    remaining_err = proc.stderr.read() if proc.stderr else ""
    proc.wait(timeout=5)
    detail = f"Program exited (code {proc.returncode}) before printing the expected output."
    if remaining_err.strip():
        detail += f"\n--- stderr ---\n{remaining_err}"
    return detail


def main():
    repo_root = Path.cwd()
    plan_path = Path(sys.argv[1]) if len(sys.argv) > 1 else repo_root / "test" / "ui-test-plan.md"
    src_dir = repo_root / "src" / "main" / "java"

    cases = parse_plan(plan_path)
    main_class = find_main_class(src_dir)

    build_dir = Path(tempfile.mkdtemp(prefix="ui-test-build-"))
    try:
        compile_program(src_dir, build_dir)

        proc = subprocess.Popen(
            ["java", "-cp", str(build_dir), main_class],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            text=True, bufsize=1,
        )

        transcript = []
        reference_divider = None
        failure = None

        # Content printed before the very first divider (should be nothing).
        leading, reference_divider = read_block(proc)
        if reference_divider is None:
            failure = ("(startup)", "the program's first divider line", describe_crash(proc))
        else:
            transcript.append(reference_divider)

        for case in cases:
            if failure:
                break
            if case.input_lines:
                proc.stdin.write(case.input_lines[0] + "\n")
                proc.stdin.flush()
                transcript.append("")
                transcript.append(case.input_lines[0])

            content, divider = read_block(proc)
            if divider is None:
                failure = (case.name, "\n".join(case.expected_lines), describe_crash(proc))
                break
            if divider != reference_divider:
                failure = (
                    case.name,
                    f"divider line: {reference_divider!r}",
                    f"divider line: {divider!r}",
                )
                break

            actual_lines = strip_blank_edges(content)
            if actual_lines != case.expected_lines:
                failure = (case.name, "\n".join(case.expected_lines), "\n".join(actual_lines))
                break

            transcript.extend(actual_lines)
            transcript.append(reference_divider)

        if proc.poll() is None:
            try:
                proc.wait(timeout=3)
            except subprocess.TimeoutExpired:
                proc.terminate()

        print("=== Console session ===")
        print("\n".join(transcript))
        print()

        if failure:
            name, expected, actual = failure
            print(f"Test result: FAILED at test case '{name}'")
            print("--- Expected ---")
            print(expected)
            print("--- Actual ---")
            print(actual)
            sys.exit(1)
        else:
            print(f"Test result: PASSED ({len(cases)} test cases)")
    finally:
        shutil.rmtree(build_dir, ignore_errors=True)


if __name__ == "__main__":
    main()
