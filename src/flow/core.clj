(ns flow.core
  (:require [clojure.tools.logging :as log]))

(def default ::default)

(def otherwise ::default)

(defn- check-predicate
  [resources data [predicate _]]
  (predicate resources data))

(defn- select-by-predicate
  [predicates resources data]
  (let [predicates-but-default (dissoc predicates ::default)
        default (get predicates ::default)
        applicables (filter (partial check-predicate resources data) predicates-but-default)
        applicables-cnt (count applicables)]
    (cond
      (= 1 applicables-cnt) (first applicables)
      (and (= 0 applicables-cnt)
           default) [::default default]
      (and (= 0 applicables-cnt)
           (nil? default)) (throw (ex-info "None of predicates matched and no default provided"
                                           {::error ::predicates-did-not-match}))
      (> 1 applicables-cnt) (throw (ex-info "More than one predicate matched"
                                            {::error ::multiple-predicates-matched})))))

(defn- get-next-node
  [graph node resources data]
  (let [node-props (get graph node)]
    (cond
      (nil? node-props) nil
      (map? node-props) (select-by-predicate node-props resources data)
      :otherwise [::forward node-props])))

(defn- execute-node
  [node resources data]
  (log/debug "Executing node" node
             "with data" data)
  (if (keyword? node)
    data
    (node resources data)))

(defn execute
  [{:keys [graph start]} resources data]
  (loop [node start
         data' data]
    (let [data'' (execute-node node resources data')
          [pred next-node] (get-next-node graph node resources data'')]
      (when next-node
        (log/debug "Predicate matched" pred
                   "next node is" next-node))
      (when-not next-node
        (log/debug "Finished"))
      (if next-node
        (recur next-node data'')
        data''))))
