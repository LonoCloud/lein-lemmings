# lein-lemmings

A Leiningen plugin to pull dependency updates automatically.

## Usage

In the `:plugins` vector of your `:user` profile:

```
[lonocloud/lein-lemmings "0.1.0-SNAPSHOT"]
```

In your `project.clj` file:

```clojure

{:lemmings
 {:repos
  [{:git "git@github.com:lonocloud/repo-a.git" :branch "master"}
   {:git "git@github.com:lonocloud/repo-b.git" :branch "master"}
   {:git "git@github.com:lonocloud/repo-c.git" :branch "master" :project-file "path-to-project.clj"}]}}
```

Run it with:

```
$ lein lemmings
```

## License

Copyright © 2015 ViaSat

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
