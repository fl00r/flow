# flow

Graph oriented programming.

An alternative way of writing code rich of conditionals.

```clojure
(def sell-booze-graph
  {get-profile-data {got-the-id? :check-age
                     flow/otherwise dont-sell}
   :check-age {is-under-21? dont-sell
               flow/otherwise get-supply}
   get-supply {is-enough-supply? :check-money
               flow/otherwise inform-not-enough-supply}
   :check-money {is-enough-money? sell
                 flow/otherwise inform-not-enough-money}
   inform-not-enough-supply inform-available-amount-of-supply
   inform-not-enough-money inform-affordable-amount-of-booze})
````

## Usage

```clojure
(require '[flow.core :as flow])

(defn- get-profile-data
  [resources data]
  (let [{:keys [id]} data
        profile (get-in resources [:profiles id])]
    (assoc data :profile profile)))

(defn- get-supply
  [resources data]
  (let [{:keys [booze]} data
        {:keys [supplies]} resources
        supply (get supplies booze)]
    (assoc data :supply supply)))

(defn- inform-not-enough-supply
  [resources data]
  (assoc data :result {:status :not-enough-booze}))

(defn- inform-not-enough-money
  [resources data]
  (assoc data :result {:status :not-enough-money}))

(defn- inform-available-amount-of-supply
  [resources data]
  (let [real-amount (get-in data [:supply :amount] 0)]
    (assoc-in data [:result :available] real-amount)))

(defn- inform-affordable-amount-of-booze
  [resources data]
  (let [{:keys [money supply]} data
        {:keys [price]} supply
        affordable-amount (int (/ money price))]
    (assoc-in data [:result :affordable] affordable-amount)))

(defn- dont-sell
  [resources data]
  (assoc data :result {:status :can-not-sell}))

(defn- sell
  [resources data]
  (assoc data :result {:status :here-you-are}))

(defn- got-the-id?
  [resources data]
  (let [{:keys [profile]} data]
    (not (nil? profile))))

(defn- is-under-21?
  [resources data]
  (let [{:keys [profile]} data
        {:keys [age]} profile]
    (< age 21)))

(defn- is-enough-supply?
  [resources data]
  (let [{:keys [amount supply]} data
        {real-amount :amount} supply]
    (and real-amount
         (>= real-amount amount))))

(defn- is-enough-money?
  [resources data]
  (let [{:keys [amount money supply]} data
        {real-amount :amount
         :keys [price]} supply
        total-price (* amount price)]
    (>= money total-price)))

(def sell-booze-graph
  {get-profile-data {got-the-id? :check-age
                     flow/otherwise dont-sell}
   :check-age {is-under-21? dont-sell
               flow/otherwise get-supply}
   get-supply {is-enough-supply? :check-money
               flow/otherwise inform-not-enough-supply}
   :check-money {is-enough-money? sell
                 flow/otherwise inform-not-enough-money}
   inform-not-enough-supply inform-available-amount-of-supply
   inform-not-enough-money inform-affordable-amount-of-booze})

(deftest get-the-booze-flow
  (let [resources {:supplies {"Beer" {:amount 10
                                      :price 7}
                              "Wine" {:amount 5
                                      :price 12}
                              "Whiskey" {:amount 2
                                         :price 22}
                              "Gin" {:amount 3
                                     :price 17}}
                   :profiles {"Ivan" {:age 30}
                              "Andrew" {:age 30}
                              "Eva" {:age 2}}}]
    (testing "all good"
      (let [data {:id "Ivan"
                  :money 100
                  :booze "Beer"
                  :amount 3}
            {:keys [result]} (flow/execute {:graph sell-booze-graph
                                            :start get-profile-data}
                                           resources
                                           data)]
        (is (= {:status :here-you-are}
               result))))
    (testing "not enough booze"
      (let [data {:id "Ivan"
                  :money 100
                  :booze "Wine"
                  :amount 10}
            {:keys [result]} (flow/execute {:graph sell-booze-graph
                                            :start get-profile-data}
                                           resources
                                           data)]
        (is (= {:status :not-enough-booze
                :available 5}
               result))))
    (testing "not enough money"
      (let [data {:id "Andrew"
                  :money 20
                  :booze "Beer"
                  :amount 5}
            {:keys [result]} (flow/execute {:graph sell-booze-graph
                                            :start get-profile-data}
                                           resources
                                           data)]
        (is (= {:status :not-enough-money
                :affordable 2}
               result))))
    (testing "no ID"
      (let [data {:id "Ira"
                  :money 1000
                  :booze "Whiskey"
                  :amount 30}
            {:keys [result]} (flow/execute {:graph sell-booze-graph
                                            :start get-profile-data}
                                           resources
                                           data)]
        (is (= {:status :can-not-sell}
               result))))
    (testing "too young"
      (let [data {:id "Eva"
                  :money 50
                  :booze "Whiskey"
                  :amount 1}
            {:keys [result]} (flow/execute {:graph sell-booze-graph
                                            :start get-profile-data}
                                           resources
                                           data)]
        (is (= {:status :can-not-sell}
               result))))))
```