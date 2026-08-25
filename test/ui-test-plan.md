# UI Test Plan

This file drives the `test-ui` skill. All test cases below run against a
**single continuous session** of the program (state carries over between
cases — a task added in an earlier case is still there for a later `list`
or `mark` case). Test cases must stay in the order they should be run in.

Each test case has:

- **Aim** — what the case is checking.
- **Input** — the line typed at this step, as a fenced code block. Empty
  for the startup case, which checks the program's output before any input
  is given.
- **Expected Output** — the exact lines the program must print in response,
  as a fenced code block. Do not include divider lines (`____...`) or
  leading/trailing blank lines — the runner adds that framing itself and
  checks it separately from the content.

Negative (error) cases are interleaved with the positive cases that change
state, and are followed by a `list` check where useful, so that a negative
case which incorrectly mutates state (e.g. adds a task it should have
rejected) shows up immediately as a wrong task count or wrong task line,
rather than being masked by a later positive case.

## Preconditions

The program loads its saved tasks from `data/ff15.txt` on startup, so the
plan assumes that file is **empty or absent** before the run — otherwise the
first `list` case would show leftover tasks. Delete it before running:

```bash
rm -f data/ff15.txt
```

A complete run ends with every task deleted, so the file is left empty and
the next run starts clean. A run that stops early (at a failing case) leaves
tasks behind, so delete the file again before re-running.

If the file exists but is corrupted (an unknown task type, or a line missing
fields), the startup block prints two extra lines after the greeting — e.g.
`AYY!!! Couldn't read your saved tasks: ...` followed by
`Starting you off with an empty list.` — and the session continues with an
empty list. That case can't be covered here, since the runner starts the
program itself and each plan runs as one session; check it by hand by
writing a bad line into `data/ff15.txt` and starting the program.

## Test Case: Startup
**Aim:** The program prints its banner and greeting before any input is given.
**Input:**
```
```
**Expected Output:**
```
 _____ _____ _  ____  
|  ___|  ___/ |/ ___| 
| |_  | |_  | |\___ \ 
|  _| |  _| | | ___) |
|_|   |_|   |_||____/ 

     Eh hello bro, I'm FF15 !
     What can I do for you big man ?
```

## Test Case: Unknown command
**Aim:** Input that isn't a recognised command word reports an error instead of being silently added as a task.
**Input:**
```
blah
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: List with an unexpected argument
**Aim:** `list` only matches the bare word — `list now` isn't recognised, since the code checks `input.equals("list")` with no `startsWith` fallback.
**Input:**
```
list now
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: List on an empty list
**Aim:** `list` before any task has been added prints only the header line, confirming the two negative cases above didn't add anything.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
```

## Test Case: Todo with no description
**Aim:** `todo` with nothing after it reports an empty-description error.
**Input:**
```
todo
```
**Expected Output:**
```
     AYY!!! The description of a todo can't be empty, bro.
```

## Test Case: Word that merely starts with "todo"
**Aim:** `todox ...` must not be treated as `todo` — `startsWith("todo ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
todox hello
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Add a todo
**Aim:** `todo <description>` adds a Todo and confirms it with the task count.
**Input:**
```
todo read book
```
**Expected Output:**
```
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
```

## Test Case: List after adding one todo
**Aim:** `list` shows exactly the one todo, confirming none of the preceding negative cases left a stray task behind.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
```

## Test Case: Deadline with no /by
**Aim:** `deadline <description>` without a `/by` reports an error explaining the required format.
**Input:**
```
deadline return book
```
**Expected Output:**
```
     AYY!!! A deadline needs a /by, e.g.: deadline return book /by 2019-12-02 1800
```

## Test Case: Deadline with no description at all
**Aim:** `deadline /by Sunday` (description omitted, `/by` immediately after the command) has no leading space before `/by` once the command word is stripped, so the `" /by"` search fails to match and this reports the generic missing-`/by` error rather than an empty-description error — documenting that edge in the parsing logic.
**Input:**
```
deadline /by Sunday
```
**Expected Output:**
```
     AYY!!! A deadline needs a /by, e.g.: deadline return book /by 2019-12-02 1800
```

## Test Case: Deadline with empty /by
**Aim:** `deadline <description> /by` with nothing after `/by` reports an empty-by error.
**Input:**
```
deadline return book /by
```
**Expected Output:**
```
     AYY!!! The /by date/time of a deadline can't be empty, bro.
```

## Test Case: Word that merely starts with "deadline"
**Aim:** `deadlinex ...` must not be treated as `deadline` — `startsWith("deadline ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
deadlinex return book /by 2019-12-02
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Deadline with a /by that isn't a date
**Aim:** A `/by` in the old free-text style is now rejected, since the date is parsed into a `LocalDate` instead of being stored as a String.
**Input:**
```
deadline return book /by Sunday
```
**Expected Output:**
```
     AYY!!! 'Sunday' isn't a date I understand. Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, e.g. 2019-12-02 or 2019-12-02 1800
```

## Test Case: Deadline with a well-formed but impossible date
**Aim:** `2019-13-45` matches the yyyy-mm-dd shape but has no such month or day, so the parser rejects it too — confirming the check is a real date parse, not just a pattern match.
**Input:**
```
deadline return book /by 2019-13-45
```
**Expected Output:**
```
     AYY!!! '2019-13-45' isn't a date I understand. Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, e.g. 2019-12-02 or 2019-12-02 1800
```

## Test Case: Add a deadline
**Aim:** `deadline <description> /by <yyyy-mm-dd>` adds a Deadline, tagged `[D]`, with the date parsed into a `LocalDate` and printed back in `MMM dd yyyy` form rather than as the text that was typed.
**Input:**
```
deadline return book /by 2019-12-02
```
**Expected Output:**
```
     Got it. I've added this task:
       [D][ ] return book (by: Dec 02 2019)
     Now you have 2 tasks in the list.
```

## Test Case: List after adding a deadline
**Aim:** `list` shows both tasks in order, confirming the four negative deadline cases above didn't add or corrupt anything.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: Dec 02 2019)
```

## Test Case: Event with no /from or /to
**Aim:** `event <description>` without `/from`/`/to` reports an error explaining the required format.
**Input:**
```
event project meeting
```
**Expected Output:**
```
     AYY!!! An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600
```

## Test Case: Event with /to before /from
**Aim:** `/to` appearing before `/from` is rejected, since the code requires `toIndex` to come after `fromIndex`.
**Input:**
```
event project meeting /to 2019-12-06 /from 2019-12-05
```
**Expected Output:**
```
     AYY!!! An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600
```

## Test Case: Event with /from but no /to
**Aim:** Supplying only `/from` still reports the combined needs-both error.
**Input:**
```
event project meeting /from 2019-12-05
```
**Expected Output:**
```
     AYY!!! An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600
```

## Test Case: Event with /to but no /from
**Aim:** Supplying only `/to` still reports the combined needs-both error.
**Input:**
```
event project meeting /to 2019-12-06
```
**Expected Output:**
```
     AYY!!! An event needs /from and /to, e.g.: event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600
```

## Test Case: Event with empty /from
**Aim:** `event <description> /from /to <to>` with nothing between `/from` and `/to` reports the empty date/time error.
**Input:**
```
event project meeting /from /to 2019-12-06
```
**Expected Output:**
```
     AYY!!! The /from and /to date/times of an event can't be empty, bro.
```

## Test Case: Event with empty /to
**Aim:** `event <description> /from <from> /to` with nothing after `/to` reports the empty date/time error.
**Input:**
```
event project meeting /from 2019-12-05 /to
```
**Expected Output:**
```
     AYY!!! The /from and /to date/times of an event can't be empty, bro.
```

## Test Case: Word that merely starts with "event"
**Aim:** `eventx ...` must not be treated as `event` — `startsWith("event ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
eventx project meeting /from 2019-12-05 /to 2019-12-07
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Event that ends before it starts
**Aim:** A `/to` date earlier than the `/from` date is rejected, since an event can't finish before it begins.
**Input:**
```
event project meeting /from 2019-12-09 /to 2019-12-08
```
**Expected Output:**
```
     AYY!!! An event can't end before it starts, bro.
```

## Test Case: Event with a /from that isn't a date
**Aim:** Event dates go through the same parser as deadline dates, so free text is rejected the same way.
**Input:**
```
event project meeting /from Monday /to 2019-12-06
```
**Expected Output:**
```
     AYY!!! 'Monday' isn't a date I understand. Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, e.g. 2019-12-02 or 2019-12-02 1800
```

## Test Case: Add an event
**Aim:** `event <description> /from <yyyy-mm-dd> /to <yyyy-mm-dd>` adds an Event, tagged `[E]`, with both dates parsed into `LocalDate`s and printed back in `MMM dd yyyy` form. It spans three days so that the `on` cases below can check a query for a day in the middle of an event.
**Input:**
```
event project meeting /from 2019-12-05 /to 2019-12-07
```
**Expected Output:**
```
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
     Now you have 3 tasks in the list.
```

## Test Case: List after adding all three types
**Aim:** `list` shows the todo, deadline, and event with their distinct type tags and suffixes, confirming the eight negative event cases above didn't add or corrupt anything.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: Dec 02 2019)
     3.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Query a day with one deadline on it
**Aim:** `on <yyyy-mm-dd>` lists the tasks falling on that single day — here only the deadline, since the event hasn't started and the todo has no date at all.
**Input:**
```
on 2019-12-02
```
**Expected Output:**
```
     Here are the tasks on Dec 02 2019:
     1.[D][ ] return book (by: Dec 02 2019)
```

## Test Case: Query a day in the middle of an event
**Aim:** Dec 06 is neither the event's start nor its end, but the event is still running that day, so it matches — confirming the check is an overlap, not an equality test on the two end dates.
**Input:**
```
on 2019-12-06
```
**Expected Output:**
```
     Here are the tasks on Dec 06 2019:
     1.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Query a whole month
**Aim:** `on <yyyy-mm>` widens the span to the whole month, so both dated tasks match. The todo is still excluded, having no date.
**Input:**
```
on 2019-12
```
**Expected Output:**
```
     Here are the tasks on Dec 2019:
     1.[D][ ] return book (by: Dec 02 2019)
     2.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Query a whole year
**Aim:** `on <yyyy>` widens the span again to the whole year, with the results kept in list order.
**Input:**
```
on 2019
```
**Expected Output:**
```
     Here are the tasks on 2019:
     1.[D][ ] return book (by: Dec 02 2019)
     2.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Query a date with nothing on it
**Aim:** A day no task falls on reports that plainly, rather than printing a bare header that would look like a bug.
**Input:**
```
on 2020-01-01
```
**Expected Output:**
```
     You've got nothing on Jan 01 2020, bro.
```

## Test Case: Query with no date at all
**Aim:** `on` with nothing after it asks for one, listing all three accepted forms.
**Input:**
```
on
```
**Expected Output:**
```
     AYY!!! Tell me when, e.g.: on 2019-12-02, on 2019-12, or on 2019
```

## Test Case: Query that isn't a date at all
**Aim:** Free text after `on` is rejected with the same three-forms hint.
**Input:**
```
on nonsense
```
**Expected Output:**
```
     AYY!!! 'nonsense' isn't a date, month, or year I understand. Try: on 2019-12-02, on 2019-12, or on 2019
```

## Test Case: Query with an impossible month
**Aim:** `2019-13` has the yyyy-mm shape but no 13th month, so the parse fails rather than matching on shape alone.
**Input:**
```
on 2019-13
```
**Expected Output:**
```
     AYY!!! '2019-13' isn't a date, month, or year I understand. Try: on 2019-12-02, on 2019-12, or on 2019
```

## Test Case: Query with too many date parts
**Aim:** Four dash-separated parts match none of the day/month/year shapes, so the query is rejected instead of being silently truncated to a day.
**Input:**
```
on 2019-12-02-05
```
**Expected Output:**
```
     AYY!!! '2019-12-02-05' isn't a date, month, or year I understand. Try: on 2019-12-02, on 2019-12, or on 2019
```

## Test Case: Word that merely starts with "on"
**Aim:** `once 2019` must not be treated as `on` — `startsWith("on ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
once 2019
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Mark with no task number
**Aim:** `mark` with nothing after it reports an error instead of crashing.
**Input:**
```
mark
```
**Expected Output:**
```
     AYY!!! Bro Tell me which task number, e.g. mark 2.
```

## Test Case: Mark with a non-numeric task number
**Aim:** `mark <non-number>` reports an error naming the bad argument.
**Input:**
```
mark abc
```
**Expected Output:**
```
     AYY!!! 'abc' aint looking like a task number.
```

## Test Case: Mark with a decimal task number
**Aim:** `mark 1.5` isn't a valid integer, so it hits the same non-numeric error path as letters.
**Input:**
```
mark 1.5
```
**Expected Output:**
```
     AYY!!! '1.5' aint looking like a task number.
```

## Test Case: Mark with task number zero
**Aim:** `mark 0` parses fine as an integer but fails the range check, reporting the out-of-range error rather than crashing on `list.get(-1)`.
**Input:**
```
mark 0
```
**Expected Output:**
```
     AYY!!! I don't have task number 0. You've got 3 task(s).
```

## Test Case: Mark with a negative task number
**Aim:** `mark -1` parses fine (`Integer.parseInt` accepts a leading minus) but fails the range check.
**Input:**
```
mark -1
```
**Expected Output:**
```
     AYY!!! I don't have task number -1. You've got 3 task(s).
```

## Test Case: Mark with an out-of-range task number
**Aim:** `mark <n>` beyond the list size reports an error stating how many tasks exist.
**Input:**
```
mark 9
```
**Expected Output:**
```
     AYY!!! I don't have task number 9. You've got 3 task(s).
```

## Test Case: Word that merely starts with "mark"
**Aim:** `marking 1` must not be treated as `mark` — `startsWith("mark ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
marking 1
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Mark task 1 as done
**Aim:** `mark <n>` marks the n-th task done and echoes it.
**Input:**
```
mark 1
```
**Expected Output:**
```
     You are cooking! I've marked this task as done:
       [T][X] read book
```

## Test Case: List after marking task 1
**Aim:** `list` shows only task 1 as done, confirming the six negative mark cases above didn't mark or otherwise change task 2 or 3.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: Dec 02 2019)
     3.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Unmark with no task number
**Aim:** `unmark` with nothing after it reports an error instead of crashing.
**Input:**
```
unmark
```
**Expected Output:**
```
     AYY!!! Bro Tell me which task number, e.g. mark 2.
```

## Test Case: Unmark with a non-numeric task number
**Aim:** `unmark <non-number>` reports an error naming the bad argument, reusing the same parser as `mark`.
**Input:**
```
unmark xyz
```
**Expected Output:**
```
     AYY!!! 'xyz' aint looking like a task number.
```

## Test Case: Unmark with an out-of-range task number
**Aim:** `unmark <n>` beyond the list size reports an error instead of crashing.
**Input:**
```
unmark 9
```
**Expected Output:**
```
     AYY!!! I don't have task number 9. You've got 3 task(s).
```

## Test Case: Word that merely starts with "unmark"
**Aim:** `unmarking 1` must not be treated as `unmark` — `startsWith("unmark ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
unmarking 1
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Unmark task 1
**Aim:** `unmark <n>` reverses the done status and echoes it.
**Input:**
```
unmark 1
```
**Expected Output:**
```
     OK, I've marked this task as not done yet:
       [T][ ] read book
```

## Test Case: List after mark/unmark round-trip
**Aim:** `list` shows task 1 back to not-done, confirming the round trip — and the three negative unmark cases above — left no side effects on the other tasks.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: Dec 02 2019)
     3.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Delete with no task number
**Aim:** `delete` with nothing after it reports an error instead of crashing.
**Input:**
```
delete
```
**Expected Output:**
```
     AYY!!! Bro Tell me which task number, e.g. mark 2.
```

## Test Case: Delete with a non-numeric task number
**Aim:** `delete <non-number>` reports an error naming the bad argument, reusing the same parser as `mark`/`unmark`.
**Input:**
```
delete abc
```
**Expected Output:**
```
     AYY!!! 'abc' aint looking like a task number.
```

## Test Case: Delete with an out-of-range task number
**Aim:** `delete <n>` beyond the list size reports an error instead of crashing.
**Input:**
```
delete 9
```
**Expected Output:**
```
     AYY!!! I don't have task number 9. You've got 3 task(s).
```

## Test Case: Word that merely starts with "delete"
**Aim:** `deletex 2` must not be treated as `delete` — `startsWith("delete ")` requires the trailing space, so this should fall through to the unknown-command error.
**Input:**
```
deletex 2
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: List before deleting
**Aim:** `list` still shows all three tasks, confirming the four negative delete cases above didn't remove anything.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: Dec 02 2019)
     3.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Delete task 2
**Aim:** `delete <n>` removes the n-th task and echoes it along with the new task count.
**Input:**
```
delete 2
```
**Expected Output:**
```
     Noted. I've removed this task:
       [D][ ] return book (by: Dec 02 2019)
     Now you have 2 tasks in the list.
```

## Test Case: List after deleting task 2
**Aim:** `list` shows the remaining two tasks, renumbered, confirming the deadline was removed and the other tasks shifted down correctly.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Delete task 1
**Aim:** Deleting down further shrinks the list by one and renumbers the remaining task, continuing to drain the list toward the empty-list edge case below.
**Input:**
```
delete 1
```
**Expected Output:**
```
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
```

## Test Case: List with one task left
**Aim:** `list` shows the sole remaining task renumbered to 1, confirming the previous delete removed the right one.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
```

## Test Case: Delete the last task
**Aim:** Deleting the final task empties the list, reporting a count of 0.
**Input:**
```
delete 1
```
**Expected Output:**
```
     Noted. I've removed this task:
       [E][ ] project meeting (from: Dec 05 2019 to: Dec 07 2019)
     Now you have 0 tasks in the list.
```

## Test Case: List on an empty list after deletions
**Aim:** `list` after deleting every task prints only the header line, same as the very first empty-list case, confirming deletion doesn't leave stray entries behind.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
```

## Test Case: Mark on an empty list
**Aim:** `mark 1` on an empty list reports the out-of-range error with a count of 0 task(s), rather than crashing on `list.get(0)`.
**Input:**
```
mark 1
```
**Expected Output:**
```
     AYY!!! I don't have task number 1. You've got 0 task(s).
```

## Test Case: Delete on an empty list
**Aim:** `delete 1` on an empty list reports the same out-of-range error with a count of 0 task(s), rather than crashing.
**Input:**
```
delete 1
```
**Expected Output:**
```
     AYY!!! I don't have task number 1. You've got 0 task(s).
```

## Test Case: Add a deadline with a time
**Aim:** `/by <yyyy-mm-dd> <HHmm>` pins the deadline to a time of day, shown after the date. The list is empty again at this point, so the count restarts at 1.
**Input:**
```
deadline return book /by 2019-12-02 1800
```
**Expected Output:**
```
     Got it. I've added this task:
       [D][ ] return book (by: Dec 02 2019, 6:00pm)
     Now you have 1 tasks in the list.
```

## Test Case: Deadline with an impossible time
**Aim:** `1860` has the HHmm shape but there is no 60th minute, so it is rejected — the same shape-plus-value check the date half gets.
**Input:**
```
deadline return book /by 2019-12-02 1860
```
**Expected Output:**
```
     AYY!!! '2019-12-02 1860' isn't a date I understand. Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, e.g. 2019-12-02 or 2019-12-02 1800
```

## Test Case: Deadline with a time in the wrong format
**Aim:** A time must be typed as HHmm, so `6pm` is rejected even though the date beside it is valid.
**Input:**
```
deadline return book /by 2019-12-02 6pm
```
**Expected Output:**
```
     AYY!!! '2019-12-02 6pm' isn't a date I understand. Write it as yyyy-mm-dd, or yyyy-mm-dd HHmm to add a time, e.g. 2019-12-02 or 2019-12-02 1800
```

## Test Case: Add an event with times
**Aim:** An event can carry a time at each end, letting it start and finish on the same day.
**Input:**
```
event project meeting /from 2019-12-05 1400 /to 2019-12-05 1600
```
**Expected Output:**
```
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 4:00pm)
     Now you have 2 tasks in the list.
```

## Test Case: Event that ends earlier the same day
**Aim:** Both ends fall on the same date, so only the times distinguish them — this is rejected, confirming the ordering check compares the time as well as the date.
**Input:**
```
event project meeting /from 2019-12-05 1600 /to 2019-12-05 1400
```
**Expected Output:**
```
     AYY!!! An event can't end before it starts, bro.
```

## Test Case: List tasks that carry times
**Aim:** `list` shows both timed tasks, confirming the two negative cases above added nothing.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[D][ ] return book (by: Dec 02 2019, 6:00pm)
     2.[E][ ] project meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 4:00pm)
```

## Test Case: Query a day for a task that carries a time
**Aim:** `on` still matches by day, ignoring the time of day, so a timed event is found by a plain date query.
**Input:**
```
on 2019-12-05
```
**Expected Output:**
```
     Here are the tasks on Dec 05 2019:
     1.[E][ ] project meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 4:00pm)
```

## Test Case: Delete the timed deadline
**Aim:** Removes the timed deadline, draining the list back toward empty so the plan leaves an empty save file behind.
**Input:**
```
delete 1
```
**Expected Output:**
```
     Noted. I've removed this task:
       [D][ ] return book (by: Dec 02 2019, 6:00pm)
     Now you have 1 tasks in the list.
```

## Test Case: Delete the timed event
**Aim:** Removes the last task, leaving the list — and so the save file — empty at the end of the run.
**Input:**
```
delete 1
```
**Expected Output:**
```
     Noted. I've removed this task:
       [E][ ] project meeting (from: Dec 05 2019, 2:00pm to: Dec 05 2019, 4:00pm)
     Now you have 0 tasks in the list.
```

## Test Case: Command word is case-sensitive
**Aim:** `Todo ...` (capital T) doesn't match the lowercase `todo` command, so it should be rejected as unknown rather than silently adding a task.
**Input:**
```
Todo not a command
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Leading whitespace on a command
**Aim:** ` list` (a leading space before an otherwise valid command) doesn't match `list`, since the raw input line isn't trimmed before the `equals`/`startsWith` checks — documenting that the parser is strict about the whole line, not just the command word.
**Input:**
```
 list
```
**Expected Output:**
```
     AYY!!! I'm sorry big man, I don't know what that means :-(
```

## Test Case: Exit
**Aim:** `bye` prints the farewell message and ends the session.
**Input:**
```
bye
```
**Expected Output:**
```
     Okok bye bye, see you again soon !
```
