# wordle

A Clojure CLI project to help you solve NYT's Wordle (or other missing letter games). 

## Usage
Use `clj -M -m wordle.core --help` for a list of available arguments.

Generally, you can run
`clj -M -m wordle.core -p <present letters> -a <absent letters -t <_____>`
to produce a list of words that match the criteria you stipulate. 

The `-p/--present` argument accepts a string of characters contained within the word.

The `-a/--absent` argument accepts a string of characters not contained in the word.

The `-t/--pattern` argument accepts a string of underscores indicating the length of the word. Individual underscores can be replaced with a letter to indicate that we know that letter to exist at that spot in the word. The program uses the count of chracters in this argument to determine the word length we're searching for. Alternatively, the `-l/length` argument can be used to indicate the length of the word. If neither of these arguments is present, the program defaults to `5` (the typical length of a Wordle word.)

### Example:

#### Input:
`clj -M -m wordle.core -p "abc" -a "def" -t "__b__"`

`abc` = the letters we know to be in the word
`def` = the letters we know are't in the word
`__b__` = indicates it's a five-letter word, where the third letter is a b

#### Output:
```
cabal
cabby
cabin
cobra
cuban
```