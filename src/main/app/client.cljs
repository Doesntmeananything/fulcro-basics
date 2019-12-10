(ns app.client
  (:require
    ["react-number-format" :as NumberFormat]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li h3 button label]]
    [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(def ui-number-format (interop/react-factory NumberFormat))

(defsc Car [this {:car/keys [id model] :as props}]
  {:query [:car/id :car/model]
   :ident :car/id
   :initial-state {:car/id :param/id
                   :car/model :param/model}}
  (div :.ui.item
       "Model: " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defmutation make-older [{:person/keys [id]}]
  (action [{:keys [state]}]
          (swap! state update-in [:person/id id :person/age] inc)))

(defsc Person [this {:person/keys [id name cars age] :as props}]
  {:query [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident :person/id
   :initial-state {:person/id :param/id
                   :person/name :param/name
                   :person/age 20
                   :person/cars [{:id 40 :model "Leaf"}
                                 {:id 41 :model "Escort"}
                                 {:id 42 :model "Sienna"}]}}
  (div :.ui.segment
       (div :.ui.form
            (div :.field
                 (label "Name: ")
                 name)
            (div :.field
                 (label "Amount: ")
                 (ui-number-format {:thousandSeparator true
                                    :prefix "$"}))
            (div :.field
                 (label "Age: ")
                 age)
            (button :.ui.button {:onClick #(comp/transact! this [(make-older {:person/id id})])} "Make Older")
            (h3 "Cars")
            (div :.ui.list
                 (map ui-car cars)))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query [{:person-list/people (comp/get-query Person)}]
   :ident (fn [_ _] [:component/id ::person-list])
   :initial-state {:person-list/people [{:id 1 :name "Bob"}
                                        {:id 2 :name "Sally"}]}}
  (div
    (h3 :.ui.header "People")
    (map ui-person people)))

(def ui-person-list (comp/factory PersonList))

(defonce app (app/fulcro-app))

(defsc Root [this {:root/keys [people]}]
  {:query [{:root/people (comp/get-query PersonList)}]
   :initial-state {:root/people {}}}
  (div
    (when people
      (ui-person-list people))))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function.
  See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! app Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! app Root "app"))

(comment
  (comp/get-initial-state Root))