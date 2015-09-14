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

(defn discover-repos [repos]
  (sh "rm" "-rf" "target")
  (println "Discovered the following repositories:")
  (println)
  (doseq [r repos]
    (println "- " (:git r))))

(defn update-local-repos [repos]
  (println)
  (println "Cloning and updating local copies...")
  (doseq [r repos]
    (println "== " (:git r) " ==")
    (u/clone-and-pull (:git r) (or (:branch r) "master"))))

(defn advance-voom-dependencies [r]
  (println "== " (:git r) " ==")
  (doseq [v (tracked-by-voom (u/project-path r (u/repo-dir (:git r))))]
    (println (meta v))
    (println v)
    (let [version (newest-version (first v))
          project-file (u/project-path r (u/repo-dir (:git r)))]
      (println "Latest version is: " version)
      (println (format "Updating project file at %s..." project-file))
      (d/update-dependency nil (str (first v)) version project-file))))

(defn test-after-dep-update [project r]
  (let [script (:test-script (:lemmings project))
        test-file (format "%s/%s" (System/getProperty "user.dir") script)
        project-file (or (:project-file r) "project.clj")
        rets (sh test-file (u/repo-dir (:git r)) (:test-cmd r))]
    (if (zero? (:exit rets))
      (do (println "Tests succeeded. Pushing new versions...")
          (u/git (u/repo-dir (:git r)) "add" project-file)
          (u/git (u/repo-dir (:git r)) "commit" "-m" "Automatically updated Voom dependencies to latest.")
          (u/git (u/repo-dir (:git r)) "push" "origin" (or (:branch r) "master")))
      (println "Tests failed. Rolling back changes..."))
    (println "Dropping any transient repository changes...")
    (u/git (u/repo-dir (:git r)) "reset" "--hard" "HEAD")
    (println)))

(defn lemmings
  "Polls binary artifacts for automatic dependency updates."
  [project & args]
  (let [repos (:repos (:lemmings project))]
    (discover-repos repos)
    (while true
      (update-local-repos repos)
      (println "Finding :dependencies tracked by Voom...")
      (doseq [r repos]
        (advance-voom-dependencies r)
        (println "Finished updating dependencies. Testing...")
        (test-after-dep-update project r))
      (println "done, sleeping until next scheduled iteration")
      (Thread/sleep (:sleep (:lemmings project))))))