(ns wordle.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [wordle.solver :as solver]))

(def cli-options
  [["-p" "--present LETTERS" "Letters that are present in the word"
    :validate [#(re-matches #"[a-zA-Z]+" %) "Must be letters only"]]
   ["-a" "--absent LETTERS" "Letters that are not present in the word"
    :validate [#(re-matches #"[a-zA-Z]+" %) "Must be letters only"]]
   ["-t" "--pattern PATTERN" "Pattern of known letters (e.g., 'a____')"
    :validate [#(re-matches #"[a-zA-Z_]+" %) "Must contain only letters or underscores"]]
   ["-l" "--length LENGTH" "Word length (defaults to 5 or pattern length)"
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a positive number"]]
   ["-w" "--wordlist PATH" "Path to word list file"
    :default "resources/words.txt"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Wordle Solver"
        ""
        "Usage: clj -M -m wordle.core [options]"
        ""
        "Options:"
        options-summary
        ""
        "Examples:"
        "  clj -M -m wordle.core -p abc -a xyz -t a____"
        "  clj -M -m wordle.core --present abc --absent xyz --pattern a____"
        "  clj -M -m wordle.core -p abc -a xyz -l 6"
        "  clj -M -m wordle.core --present abc --absent xyz --pattern a_____ --length 6"]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      
      ;; custom validation on arguments
      (and (empty? (:present options))
           (empty? (:absent options))
           (not (:pattern options)))
      {:exit-message "Error: At least one of --present, --absent, or --pattern must be provided."}
      
      ;; validate pattern length matches specified length if both are provided
      (and (:pattern options) (:length options)
           (not= (count (:pattern options)) (:length options)))
      {:exit-message (format "Error: Pattern length (%d) does not match specified word length (%d)"
                            (count (:pattern options)) (:length options))}
      
      :else ; passed custom validation => proceed with program
      {:options options})))

(defn -main
  "Wordle solver CLI"
  [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)
        word-length (or (:length options)
                       (when (:pattern options)
                         (count (:pattern options)))
                       5)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (let [words (solver/load-and-filter-words (:wordlist options) word-length)
            matches (solver/find-matching-words words
                                              (:pattern options)
                                              (:present options)
                                              (:absent options))]
        (if (empty? matches)
          (println "No matching words found.")
          (do
            (println (format "Found %d matching words:" (count matches)))
            (doseq [word matches]
              (println word))))))))
