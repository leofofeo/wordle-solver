(ns wordle.solver
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn load-and-filter-words
  "Returns words of specified length from file as a set"
  [file-path word-length]
  (with-open [reader (io/reader file-path)]
    (->> (line-seq reader)
         (map str/lower-case)
         (filter #(= word-length (count %)))
         set)))


(defn parse-pattern
  "Convert pattern string (e.g., 'a____' into a map of position -> letter)"
  [pattern]
  (when pattern
    (->> pattern
         str/lower-case
         (map-indexed (fn [idx c] [idx c]))
         (filter (fn [[_ c]] (not= c \_)))
         (into {}))))

(defn matches-pattern?
  "Check if a word matches the given pattern"
  [word pattern-map]
  (or (empty? pattern-map)
      (every? (fn [[pos letter]]
                (= (nth word pos) letter))
              pattern-map)))

(defn contains-all-letters?
  "Check if a word contains all the required letters"
  [word required-letters]
  (or (empty? required-letters)
      (let [word-set (set word)]
        (every? word-set required-letters))))

(defn contains-none-letters?
  "Check if a word contains none of the excluded letters"
  [word excluded-letters]
  (or (empty? excluded-letters)
      (let [word-set (set word)]
        (not-any? word-set excluded-letters))))

(defn find-matching-words
  "Find words that match the given criteria"
  [words pattern present-letters absent-letters]
  (let [pattern-map (parse-pattern pattern)
        present-set (set (str/lower-case (or present-letters "")))
        absent-set (set (str/lower-case (or absent-letters "")))]
    (->> words
         (filter #(matches-pattern? % pattern-map))
         (filter #(contains-all-letters? % present-set))
         (filter #(contains-none-letters? % absent-set))
         sort)))