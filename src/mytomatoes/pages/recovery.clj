(ns mytomatoes.pages.recovery
  (:require [mytomatoes.layout :refer [with-layout]]
            [hiccup.core :refer [html]]
            [cheshire.core :refer [generate-string]]
            [mytomatoes.word-stats :refer [common-words]]
            [ring.util.response :refer [redirect]]
            [mytomatoes.storage :as st]
            [taoensso.timbre :refer [info]]))

(def map-of-common-words
  (->> common-words
       (map (juxt identity (constantly 1)))
       (into {})))

(defn get-page [request]
  (let [proposed-username (get-in request [:params "username"] "")
        username (if (= proposed-username "username") "" proposed-username)]
    (with-layout request
      {:body
       (html
        [:div {:id "welcome" :class "recovery"}
         (if (= "invalid" (get-in request [:params "code"]))
           [:p "Whoa, that code to change your password was wrong, for some
              reason. Damn. Let's try again. Type in four words that you've used
              in your tomatoes. If they all match, we'll call that good enough.
              Okay?"]
           (list
            [:p "Lost your password? Let's see if we can fix that. Type in four
              words that you've used in your tomatoes. If they all match, we'll
              call that good enough. Okay?"]
            [:p "Think back. What have you been doing? What sort of description
              did you write for your tomatoes? Did you use any special names?
              Abbreviations? Words specific to your field? Type them in one at a
              time."]))
         [:form {:id "the-form"}
          [:div {:id "fields"}
           [:div {:class "mas"} [:label.strong {:for "username"} "your username:"]]
           [:input {:type "text" :id "username" :name "username" :value username}]
           [:div {:class "mas"} [:label.strong {:for "word1"} "four words:"]]
           [:input {:type "text" :id "word1" :name "word1" :class "word"}]
           [:input {:type "text" :id "word2" :name "word2" :class "word"}]
           [:input {:type "text" :id "word3" :name "word3" :class "word"}]
           [:input {:type "text" :id "word4" :name "word4" :class "word"}]]
          [:input {:type "submit" :id "submit" :value "loading..." :disabled true}]]]
        [:script
         "var MT = {};"
         "MT.commonWords = " (generate-string map-of-common-words) ";"])
       :script-bundles ["recovery.js"]})))

(defn get-change-password-page [request]
  (if-let [code (get-in request [:params "code"])]
    (if-let [account-id (st/get-account-id-by-remember-code (:db request) code)]
      (with-layout request
        {:body
         (html
          [:div {:id "welcome"}
           [:p "Please type your new password a couple times, and we can get back to doing some real work. :-)"]
           [:form {:id "the-form"}
            [:input {:type "hidden" :name "code" :value code}]
            [:h3 "Change password"]
            [:div {:id "fields"}
             [:input {:type "password" :id "password" :name "password"}]
             [:input {:type "password" :id "password2" :name "password2"}]]
            [:input {:type "submit" :id "submit" :value "loading..." :disabled true}]]])
         :script-bundles ["change-password.js"]})
      (do
        (info "Change password page visited with invalid code: " code)
        (redirect "/recovery?code=invalid")))
    (do
      (info "Change password page visited without a code.")
      (redirect "/recovery"))))
