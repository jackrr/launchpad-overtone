(defproject launchpad-overtone "0.1.0-SNAPSHOT"
  :description "Interface to Launchpad X"
  :url "https://github.com/jackrr/launchpad-overtone"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [mount/mount "0.1.23"]
                 [overtone/overtone "0.16.3331"]]
  :repl-options {:init-ns launchpad-overtone.core})
