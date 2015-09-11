(defproject lonocloud/lein-lemmings "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[lonocloud/lein-unison "0.1.11"]
                 [lein-update-dependency "0.1.2"]
                 [rewrite-clj "0.4.12"]
                 [ancient-clj "0.3.11"]]
  :eval-in-leiningen true
  :lemmings
  {:repos
   [{:git "git@github.com:onyx-platform/onyx-kafka.git" :branch "master"}
    {:git "git@github.com:onyx-platform/onyx-datomic.git" :branch "master"}
    {:git "git@github.com:onyx-platform/onyx-sql.git" :branch "master"}]})
