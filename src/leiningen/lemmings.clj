(ns leiningen.lemmings
  (:require [leiningen.unison :as u]
            [leiningen.update-dependency :as d]
            [ancient-clj.core :as a]
            [rewrite-clj.zip :as z]))

(defn newest-version [dep]
  (first
   (map :version-string
        (a/versions! [dep]))))

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
    (doseq [r repos]
      (println (:git r)))
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
        (let [version (newest-version (first v))]
          (println "Latest version is: " version)
          (println "Updating project file...")
          (d/update-dependency nil (str (first v)) version (u/project-path r (u/repo-dir (:git r)))))
        (println)))
    (println "done")))