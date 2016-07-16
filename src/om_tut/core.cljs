(ns om-tut.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(println "This text is printed from src/om-tut/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state
  (atom {:app/title "Animals"
         :animals/list [[1 "ant"] [2 "antelope"] [3 "bird"] [4 "Cat"] [5 "Dog"]
      [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]}))

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [frame @state]
    (if-let [[_ value] (find frame key)]
      {:value value}
      {:value :not-found})))

(defmethod read :animals/list
  [{:keys [state] :as env} key {:keys [start end]}]
  {:value (subvec (:animals/list @state) start end)})

(defui AnimalsList
  static om/IQueryParams
  (params [this]
          {:start 0 :end 10})
  static om/IQuery
  (query [this]
         '[:app/title (:animals/list {:start ?start :end ?end})])
  Object
  (render [this]
          (let [{:keys [app/title animals/list]} (om/props this)]
            (dom/div nil
                     (dom/h2 nil title)
                     (apply dom/ul nil
                            (map (fn [[i name]]
                                   (println "i " i " name " name)
                                   (dom/li nil (str i ". " name)))
                                 list))))))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read read})}))

(om/add-root! reconciler AnimalsList (gdom/getElement "app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  (println (str "figwheel counter: ", (get-in @app-state [:__figwheel_counter])))
)
