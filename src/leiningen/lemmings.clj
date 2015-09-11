(ns leiningen.lemmings
  (:require [clojure.java.shell :refer [sh]]
            [leiningen.unison :as u]
            [leiningen.update-dependency :as d]
            [ancient-clj.core :as a]
            [rewrite-clj.zip :as z]))

(defn newest-version [dep]
  (first (map :version-string (a/versions! dep))))

(defn tracked-by-voom [p]
  (let [deps
        (-> (z/of-file p)
            z/down
            (z/find-value :dependencies)
            z/right
            z/sexpr)]
    (filter
     (fn [d] (meta d))
     deps)))

(defn lemmings
  "Polls binary artifacts for automatic dependency updates."
  [project & args]
  (let [repos (:repos (:lemmings project))]
    (println "Discovered the following repositories:")
    (println)
    (sh "rm" "-rf" "target")
    (while true
      (doseq [r repos]
        (println "- " (:git r)))
      (println)
      (println "Cloning and updating local copies...")
      (doseq [r repos]
        (println "== " (:git r) " ==")
        (u/clone-and-pull (:git r) (or (:branch r) "master")))
      (println "Finding :dependencies tracked by Voom...")
      (doseq [r repos]
        (println "== " (:git r) " ==")
        (doseq [v (tracked-by-voom (u/project-path r (u/repo-dir (:git r))))]
          (println (meta v))
          (println v)
          (let [version (newest-version (first v))
                project-file (u/project-path r (u/repo-dir (:git r)))]
            (println "Latest version is: " version)
            (println (format "Updating project file at %s..." project-file))
            (d/update-dependency nil (str (first v)) version project-file)))
        (println "Finished updating dependencies. Testing...")
        (let [script (:test-script (:lemmings project))
              test-file (format "%s/%s" (System/getProperty "user.dir") script)
              status (:exit (sh test-file (u/repo-dir (:git r)) (:test-cmd r)))]
          (if (zero? status)
            (println "Tests succeeded!")
            (println "Tests failed!"))
          (println "Dropping any transient repository changes...")
          (u/git (u/repo-dir (:git r)) "reset" "--hard" "HEAD")
          (println)))
      (println "done, sleeping until next scheduled iteration")
      (Thread/sleep (:sleep (:lemmings project))))))