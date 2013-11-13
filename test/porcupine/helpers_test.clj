(ns porcupine.helpers-test
  (:require [clojure.test :refer :all]
            [porcupine.helpers :refer :all]
            [porcupine.core :as core]
            [porcupine.test-util :as test-util]))

(deftest token-substitution
  (testing "Make sure tokens are substituted in output at the right place"
    (test-util/harness (fn [resources]
              (core/add-resource resources :javascript "bing/bong.js")
              (core/add-resource resources :javascript "bing/bang.js")
              (core/add-resource resources :javascript "bing/BING!.js" {:location :header})
              (core/add-resource resources :stylesheet "zing/zong.css")
              (core/add-resource resources :stylesheet "zing/zang.css")
              (core/add-resource resources :stylesheet "zing/ZING!.css" {:location :footer})
              (let [template (str "<html><head>\n"
                                  (header-resources resources)
                                  "</head>\n<body>blah blah\n"
                                  (footer-resources resources)
                                  "</body></html>")
                    final (core/replace-tokens resources template)
                    expected "<html><head>
<link rel=\"stylesheet\" href=\"zing/zong.css\">
<link rel=\"stylesheet\" href=\"zing/zang.css\">
<script type=\"text/javascript\" src=\"bing/BING!.js\"></script></head>
<body>blah blah
<link rel=\"stylesheet\" href=\"zing/ZING!.css\">
<script type=\"text/javascript\" src=\"bing/bong.js\"></script>
<script type=\"text/javascript\" src=\"bing/bang.js\"></script></body></html>"]
                (is (= final expected)))))))

