(ns om-tut.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(println "This text is printed from src/om-tut/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(def init-data
  {:dashboard/items
   [{:id 0 :type :dashboard/post
     :author "Laura Smith"
     :title "A Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}
    {:id 1 :type :dashboard/photo
     :title "A Photo!"
     :image "photo.jpg"
     :caption "Lorem ipsum"
     :favorites 0}
    {:id 2 :type :dashboard/post
     :author "Jim Jacobs"
     :title "Another Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}
    {:id 3 :type :dashboard/graphic
     :title "Charts and Stufff!"
     :image "chart.jpg"
     :favorites 0}
    {:id 4 :type :dashboard/post
     :author "May Fields"
     :title "Yet Another Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}]})

(defui Post
  static om/IQuery
  (query [this]
    [:id :type :title :author :content])
  Object
  (render [this]
          (let [{:keys [title author content] :as props} (om/props this)]
            (dom/div nil
                     (dom/h3 nil title)
                     (dom/h4 nil author)
                     (dom/p nil content)))))

(def post (om/factory Post))

(defui Photo
  static om/IQuery
  (query [this]
    [:id :type :title :image :caption])
  Object
  (render [this]
          (let [{:keys [title image caption] :as props} (om/props this)]
            (dom/div nil
                     (dom/h3 nil (str "Photo: " title))
                     (dom/div nil image)
                     (dom/p nil (str "Caption: " caption))))))

(def photo (om/factory Photo))

(defui Graphic
  static om/IQuery
  (query [this]
    [:id :type :title :image])
  Object
  (render [this]
          (let [{:keys [title image] :as props} (om/props this)]
            (dom/div nil
                     (dom/h3 nil (str "Graphic: " title))
                     (dom/div nil image)))))

(def graphic (om/factory Graphic))

(defui DashboardItem
  static om/Ident
  (ident [this {:keys [id type]}]
    [type id])
  static om/IQuery
  (query [this]
    (zipmap
      [:dashboard/post :dashboard/photo :dashboard/graphic]
      (map #(conj % :favorites)
        [(om/get-query Post)
         (om/get-query Photo)
         (om/get-query Graphic)])))
  Object
  (render [this]
          (let [{:keys [id type favorites] :as props} (om/props this)]
            (dom/li #js {:style #js {:padding 10 :borderBottom "1px solid black"}}
                    (dom/div nil
                             (({:dashboard/post post
                                :dashboard/photo photo
                                :dashboard/graphic graphic} type)
                              (om/props this))
                             (dom/div nil
                                      (dom/p nil (str "Favorites: " favorites))
                                      (dom/button #js {:onClick
                                                       (fn [event]
                                                         (om/transact! this
                                                                       `[(dashboard/favorite {:ref [~type ~id]})]))}
                                                  "<3")))))))

(def dashboard-item (om/factory DashboardItem))

(defui Dashboard
  static om/IQuery
  (query [this]
    [{:dashboard/items (om/get-query DashboardItem)}])
  Object
  (render [this]
          (let [{:keys [dashboard/items]} (om/props this)]
            (apply dom/ul
                   #js {:style #js {:padding 0}}
                   (map dashboard-item items)))))

(defmulti read om/dispatch)

(defmethod read :dashboard/items
  [{:keys [state]} k _]
  (let [frame @state]
    {:value (into [] (map #(get-in frame %)) (get frame k))}))

(defmulti mutate om/dispatch)

(defmethod mutate 'dashboard/favorite
  [{:keys [state]} k {:keys [ref]}]
  {:action (fn []
             (swap! state update-in (conj ref :favorites) inc))})

(def reconciler
  (om/reconciler {:state init-data
                  :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler Dashboard (gdom/getElement "app"))
