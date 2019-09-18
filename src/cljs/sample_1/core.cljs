(ns sample-1.core
  (:require
    [reagent.core :as r]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample-1.ajax :as ajax]
    [ajax.core :refer [GET POST]]
    [reitit.core :as reitit]
    [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :active (when (= page (:page @session)) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample-1"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-end
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])


(def answer-1 (r/atom {:x 5 :y 18 :op "+" :total 0}))
(def answer-2 (r/atom {:x 9 :y 3 :op "+" :total 0}))
(def answer-3 (r/atom {:x 2 :y 19 :op "+" :total 0}))

(defn- set-total [a r]
  (prn "set-total" a r)
  (swap! a assoc :total (:total r)))

(defn- set-operator [a r]
  (prn "set-operator" a r)
  (swap! a assoc :op r)
  (prn "   after" a))

(defn get-answer
  [a]
  (let [x (:x @a) y (:y @a) op (:op @a)]
    (prn "posting" x y op a)
    (cond
      (= op "+") (POST "/api/math/plus"
                   {:headers {"Accept" "application/transit+json"}
                    :params {:x x :y y}
                    :handler #(set-total a %)})
      (= op "-") (POST "/api/math/minus"
                   {:headers {"Accept" "application/transit+json"}
                    :params {:x x :y y}
                    :handler #(set-total a %)})
      (= op "*") (POST "/api/math/mult"
                   {:headers {"Accept" "application/transit+json"}
                    :params {:x x :y y}
                    :handler #(set-total a %)})
      (= op "/") (POST "/api/math/div"
                   {:headers {"Accept" "application/transit+json"}
                    :params {:x x :y y}
                    :handler #(set-total a %)}))))


(defn- parse-int [x]
  ())


(defn input-field [tag id data]
  [:div.field
   [tag
    {:type :number
     :value (id @data)
     :placeholder (name id)
     :on-change #(do
                   (prn "change" id (-> % .-target .-value))
                   (swap! data
                          assoc
                          id (js/parseInt (-> % .-target .-value)))
                   (get-answer data))}]])


(defn- make-row [data]
  [:tr
   [:td [input-field :input.input :x data]]
   [:td [:select {:on-change #(do
                                (set-operator data (-> % .-target .-value))
                                (get-answer data))}
         [:option "+"]
         [:option "-"]
         [:option "*"]
         [:option "/"]]]
   [:td [input-field :input.input :y data]]
   [:td "="]
   [:td (str (:total @data))]])



(defn home-page []
  [:section.section>div.container>div.content
   [:table
    [:tbody
     (make-row answer-1)
     (make-row answer-2)
     (make-row answer-3)]]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(swap! session assoc :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (fetch-docs!)

  (get-answer answer-1)
  (get-answer answer-2)
  (get-answer answer-3)

  (hook-browser-navigation!)
  (mount-components))
