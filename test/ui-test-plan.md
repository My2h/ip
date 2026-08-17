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

## Test Case: Add a plain-text task
**Aim:** Free-text input (no command word) is still added as a task, using the older confirmation message.
**Input:**
```
return book
```
**Expected Output:**
```
     added: return book
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

## Test Case: Mark task 2 as done
**Aim:** `mark` works for a task added via the plain-text fallback too.
**Input:**
```
mark 2
```
**Expected Output:**
```
     You are cooking! I've marked this task as done:
       [X] return book
```

## Test Case: List after marking
**Aim:** `list` shows both tasks as done, with the `[T]` type tag only on the todo.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][X] read book
     2.[X] return book
```

## Test Case: Unmark task 2
**Aim:** `unmark <n>` reverses the done status and echoes it.
**Input:**
```
unmark 2
```
**Expected Output:**
```
     OK, I've marked this task as not done yet:
       [ ] return book
```

## Test Case: List after unmarking
**Aim:** `list` reflects the unmark — task 1 still done, task 2 not done.
**Input:**
```
list
```
**Expected Output:**
```
     Here are the tasks in your list:
     1.[T][X] read book
     2.[ ] return book
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
