(ns erp12.cbgp-lite.benchmark.suite.psb
  (:require [clj-fuzzy.levenshtein :as lev]
            [clojure.string :as str]
            [clojure.string]
            [erp12.cbgp-lite.benchmark.utils :as bu]
            [erp12.cbgp-lite.lang.lib :as lib]
            [erp12.cbgp-lite.search.individual :as i]
            [erp12.cbgp-lite.task :as task]
            [psb2.core :as psb2]))

(defn problems
  [{:keys [penalty]}]
  {"checksum"
   {:input->type {'input1 {:type 'string?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'int?} {:type 'boolean?} {:type 'char?}]
    :extra-genes [{:gene :lit, :val "Check sum is ", :type {:type 'string?}}
                  {:gene :lit, :val \space, :type {:type 'char?}}
                  {:gene :lit, :val 64, :type {:type 'int?}}
                  {:gene :lit-generator, :fn (bu/int-generator 128), :type {:type 'int?}}
                  {:gene :lit-generator, :fn bu/rand-char, :type {:type 'char?}}]
    :loss-fns    [lev/distance
                  #(if (not (empty? %1))
                     (Math/abs (- (int (last %2)) (int (last %1)))) ;distance from correct last character
                     penalty)]}

   "collatz-numbers"
   {:input->type {'input1 {:type 'int?}}
    :ret-type    {:type 'int?}
    :other-types [{:type 'int?} {:type 'double?} {:type 'boolean?}]
    :extra-genes [{:gene :lit, :val 0, :type {:type 'int?}}
                  {:gene :lit, :val 1, :type {:type 'int?}}
                  {:gene :lit-generator, :fn (bu/int-generator 128), :type {:type 'int?}}]
    :loss-fns    [bu/absolute-distance]}

   "compare-string-lengths"
   {:input->type {'input1 {:type 'string?}
                  'input2 {:type 'string?}
                  'input3 {:type 'string?}}
    :ret-type    {:type 'boolean?}
    :other-types [{:type 'int?}]
    :extra-genes [{:gene :lit-generator, :fn bu/rand-bool, :type {:type 'boolean?}}]
    :loss-fns    [#(if (= %1 %2) 0 1)]}

   "count-odds"
   {:input->type {'input1 {:type :vector :child {:type 'int?}}}
    :ret-type    {:type 'int?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit, :val 0, :type {:type 'int?}}
                  {:gene :lit, :val 1, :type {:type 'int?}}
                  {:gene :lit, :val 2, :type {:type 'int?}}
                  {:gene :lit-generator, :fn (bu/int-generator 1000), :type {:type 'int?}}]
    :loss-fns    [bu/absolute-distance]
    :solution    (list {:gene :local :idx 0}
                       {:gene :fn :arg-types [{:type 'int?}]}
                       {:gene :lit :val 2 :type {:type 'int?}}
                       {:gene :local :idx 1}
                       {:gene :var :name 'int-mod}
                       {:gene :apply}
                       {:gene :lit :val 1 :type {:type 'int?}}
                       {:gene :var :name '=}
                       {:gene :apply}
                       {:gene :close}
                       {:gene :var :name 'filterv}
                       {:gene :apply}
                       {:gene :var :name 'count}
                       {:gene :apply})}

   "digits"
   {:input->type {'input1 {:type 'int?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'boolean?} {:type 'char?}]
    :extra-genes [{:gene :lit, :val \newline, :type {:type 'char?}}
                  {:gene :lit, :fn (bu/int-generator 10), :type {:type 'int?}}]
    :loss-fns    [lev/distance]}

   "double-letters"
   {:input->type {'input1 {:type 'string?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'int?} {:type 'boolean?} {:type 'char?}]
    :extra-genes [{:gene :lit, :val \!, :type {:type 'char?}}]
    :loss-fns    [lev/distance]}

   ;"even-squares"

   "for-loop-index"
   {:input->type {'input1 {:type 'int?}
                  'input2 {:type 'int?}
                  'input3 {:type 'int?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'int?} {:type 'boolean?}]
    :loss-fns    [lev/distance]}

   "grade"
   {:input->type {'input1 {:type 'int?}
                  'input2 {:type 'int?}
                  'input3 {:type 'int?}
                  'input4 {:type 'int?}
                  'input5 {:type 'int?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit, :val "Student has a ", :type {:type 'string?}}
                  {:gene :lit, :val " grade.", :type {:type 'string?}}
                  {:gene :lit, :val "A", :type {:type 'string?}}
                  {:gene :lit, :val "B", :type {:type 'string?}}
                  {:gene :lit, :val "C", :type {:type 'string?}}
                  {:gene :lit, :val "D", :type {:type 'string?}}
                  {:gene :lit, :val "F", :type {:type 'string?}}
                  {:gene :lit-generator, :fn #(rand-int 101), :type {:type 'string?}}]
    :loss-fns    [lev/distance
                  ;; If correct format, distance from correct letter grade char.
                  (let [extract-letter #(second (re-find #"^Student has a (.) grade.$" %))]
                    #(let [actual-letter (extract-letter %1)
                           expected-letter (extract-letter %2)]
                       (if actual-letter
                         (Math/abs (- (int (first expected-letter))
                                      (int (first actual-letter))))
                         penalty)))]}

   "last-index-of-zero"
   {:input->type {'input1 {:type :vector :child {:type 'int?}}}
    :ret-type    {:type 'int?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit-generator, :fn (bu/int-generator 50), :type {:type 'int?}}]
    :loss-fns    [bu/absolute-distance]}

   "median"
   {:input->type {'input1 {:type 'int?}
                  'input2 {:type 'int?}
                  'input3 {:type 'int?}}
    :ret-type    {:type 'int?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit-generator, :fn (bu/int-generator 100), :type {:type 'int?}}]
    :loss-fns    [bu/absolute-distance]}

   "mirror-image"
   {:input->type {'input1 {:type :vector :child {:type 'int?}}
                  'input2 {:type :vector :child {:type 'int?}}}
    :ret-type    {:type 'boolean?}
    :other-types [{:type 'int?}]
    :extra-genes [{:gene :lit-generator, :fn bu/rand-bool, :type {:type 'int?}}]
    :loss-fns    [#(if (= %1 %2) 0 1)]
    :solution    (list {:gene :local :idx 0}
                       {:gene :local :idx 1}
                       {:gene :var :name `lib/reversev}
                       {:gene :apply}
                       {:gene :var :name '=}
                       {:gene :apply})}

   "negative-to-zero"
   {:input->type {'input1 {:type :vector :child {:type 'int?}}}
    :ret-type    {:type :vector :child {:type 'int?}}
    :other-types [{:type 'int?} {:type 'boolean?}]
    :extra-genes [{:gene :lit, :val 0, :type {:type 'int?}}]
    :loss-fns    [lev/distance]}

   "number-io"
   {:input->type    {'input1 {:type 'double?}
                     'input2 {:type 'int?}}
    :ret-type       {:type 'string?}
    :lit-generators [(bu/int-generator 100)
                     #(- (rand 201.0) 100.0)]
    :loss-fns       [#(try
                        (bu/round 4 (Math/abs (- (Double/parseDouble %1) %2)))
                        (catch Exception e penalty))
                     #(lev/distance (take 10 %1)
                                    (take 10 (pr-str %2)))]
    :solution       (list {:gene :local :idx 0}
                          {:gene :local :idx 1}
                          {:gene :var :name 'double}
                          {:gene :apply}
                          {:gene :var :name 'double-add}
                          {:gene :apply}
                          {:gene :var :name 'str}
                          {:gene :apply})}

   ; "pig-latin"

   "replace-space-with-newline"
   {:input->type {'input1 {:type 'string?}}
    :ret-type    {:type 'int?}
    ;; The `nil?` functions are side effects, like printing.
    :other-types [{:type 'int?} {:type 'boolean?} {:type 'char?} {:type 'nil?}]
    :extra-genes [{:gene :lit, :val \space, :type {:type 'char?}}
                  {:gene :lit, :val \newline, :type {:type 'char?}}
                  {:gene :lit-generator, :fn bu/rand-char, :type {:type 'char?}}]
    :loss-fns    [bu/absolute-distance]
    ;; Config for how to unpack the cases from data files.
    :out-key     :output2
    :stdout-key  :output1
    :solution    (list {:gene :lit :val \newline :type {:type 'char?}}
                       {:gene :lit :val \space :type {:type 'char?}}
                       {:gene :local :idx 0}
                       {:gene :var :name `lib/replace-char}
                       {:gene :apply}
                       {:gene :let}
                       ;; Return
                       {:gene :lit :val \newline :type {:type 'char?}}
                       {:gene :local :idx 1}
                       {:gene :var :name `lib/remove-char}
                       {:gene :apply}
                       {:gene :var :name 'length}
                       {:gene :apply}
                       ;; Print
                       {:gene :local :idx 1}
                       {:gene :var :name 'print}
                       {:gene :apply}
                       ;; Wrap 2 expressions in do
                       {:gene :var :name 'do2}
                       {:gene :apply})}

   ; "scrabble-score"

   "small-or-large"
   {:input->type {'input1 {:type 'int?}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit, :val "small", :type {:type 'string?}}
                  {:gene :lit, :val "large", :type {:type 'string?}}
                  {:gene :lit-generator, :fn (bu/int-generator 10000), :type {:type 'int?}}]
    :loss-fns    [lev/distance]}

   "smallest"
   {:input->type {'input1 {:type 'int?}
                  'input2 {:type 'int?}
                  'input3 {:type 'int?}
                  'input4 {:type 'int?}}
    :ret-type    {:type 'int?}
    :other-types [{:type 'boolean?}]
    :extra-genes [{:gene :lit-generator, :fn (bu/int-generator 100), :type {:type 'int?}}]
    :loss-fns    [bu/absolute-distance]
    :solution    [{:gene :local :idx 0}
                  {:gene :local :idx 1}
                  {:gene :local :idx 2}
                  {:gene :local :idx 3}
                  {:gene :var :name 'min-int}
                  {:gene :var :name 'min-int}
                  {:gene :var :name 'min-int}
                  {:gene :apply}
                  {:gene :apply}
                  {:gene :apply}]}

   ; "string-differences"

   "string-lengths-backwards"
   {:input->type {'input1 {:type :vector :child {:type 'string?}}}
    :ret-type    {:type 'string?}
    :other-types [{:type 'int?} {:type 'boolean?}]
    :extra-genes [{:gene :lit-generator, :fn (bu/int-generator 100), :type {:type 'int?}}]
    :loss-fns    [lev/distance]}

   ; "sum-of-squares"
   ; "super-anagrams"

   "syllables"
   {:input->type    {'input1 {:type 'string?}}
    :ret-type       {:type 'string?}
    :other-types    [{:type 'int?} {:type 'boolean?} {:type 'char?}]

    :extra-genes    [{:gene :lit, :val "The number of syllables is ", :type {:type 'string?}}
                     {:gene :lit, :val \a, :type {:type 'char?}}
                     {:gene :lit, :val \e, :type {:type 'char?}}
                     {:gene :lit, :val \i, :type {:type 'char?}}
                     {:gene :lit, :val \o, :type {:type 'char?}}
                     {:gene :lit, :val \u, :type {:type 'char?}}
                     {:gene :lit, :val \y, :type {:type 'char?}}
                     {:gene :lit, :val "aeiouy", :type {:type 'string?}}
                     {:gene :lit-generator, :fn bu/rand-char, :type {:type 'char?}}]
    :loss-fns       [lev/distance
                     (let [parse #(try (Integer/parseInt (last (str/split % #"\s+")))
                                       (catch Exception e nil))]
                       #(if-let [num (parse %1)]
                          (bu/absolute-distance num (parse %2))
                          penalty))]}

   "vector-average"
   {:input->type    {'input1 {:type :vector :child {:type 'double?}}}
    :ret-type       {:type 'double?}
    :other-types    [{:type 'int?}]
    :loss-fns       [#(bu/round 4 (bu/absolute-distance %1 %2))]}

   "vectors-summed"
   {:input->type    {'input1 {:type :vector :child {:type 'int?}}
                     'input2 {:type :vector :child {:type 'int?}}}
    :ret-type       {:type :vector :child {:type 'int?}}
    :other-types    [{:type 'int?}]
    :loss-fns       [(fn [y-hat y]
                       (reduce + (map #(or (bu/absolute-distance %1 %2) penalty)
                                      y-hat y)))
                     (fn [y-hat y]
                       (* 1000 (bu/absolute-distance (count y-hat) (count y))))]
    :solution       (list {:gene :local :idx 0}
                          {:gene :local :idx 1}
                          {:gene :var :name 'int-add}
                          {:gene :var :name 'mapv2}
                          {:gene :apply})}

   ; "wallis-pi"
   ; "word-stats"
   ; "x-word-lines"

   })

(defn reshape-case
  [case {:keys [out-key stdout-key] :or {out-key :output1}}]
  (merge
    {:inputs (->> case
                  (filter (fn [[k _]] (str/starts-with? (name k) "input")))
                  (sort-by first)
                  (mapv second))
     :output (out-key case)}
    (when stdout-key
      {:std-out (stdout-key case)})))

(defn read-cases
  [{:keys [data-dir problem n-train n-test] :as opts}]
  (let [problem-info (get (problems {}) (name problem))
        reshape #(reshape-case % problem-info)
        {:keys [train test]} (psb2/fetch-examples (str data-dir) (str problem) n-train n-test)]
    {:train (map reshape train)
     :test  (map reshape test)}))

(defn validate-solutions
  [{:keys [data-dir num-cases]}]
  (let [suite (problems {:penalty 1000})]
    (doseq [[problem-name task] (filter (fn [[_ task]] (contains? task :solution)) suite)]
      (println "Starting" problem-name)
      (let [factory (i/make-individual-factory (-> task
                                                   task/enhance-task
                                                   (assoc :evaluate-fn i/evaluate-full-behavior
                                                          :cases (:test (read-cases {:data-dir (name data-dir)
                                                                                     :problem  problem-name
                                                                                     :n-test   num-cases
                                                                                     :n-train  0})))))
            start-time (System/currentTimeMillis)
            evaluation (factory (:solution task) nil)
            duration (/ (- (System/currentTimeMillis) start-time) 1000)]
        (cond
          (> (:total-error evaluation) 0)
          (throw (ex-info (str problem-name " solution has non-zero error.") {:eval evaluation}))

          (some? (:exception evaluation))
          (throw (ex-info (str problem-name " solution threw an error.") {:eval evaluation} (:exception evaluation)))

          :else
          (println problem-name "passed in" duration "seconds."))))))


(comment

  (validate-solutions {:data-dir "data/psb/" :num-cases 10})

  )