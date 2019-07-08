(defproject flow "0.1.0-SNAPSHOT"
  :description "Flow oriented development"
  :url "http://github.com/fl00r/flow"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]]
  :repl-options {:init-ns flow.core})
