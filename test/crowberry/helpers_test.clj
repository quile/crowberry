(ns lumberg.helpers-test
  (:require [clojure.test :refer :all]
            [lumberg.helpers :refer :all]
            [lumberg.core :as core]
            [lumberg.test-util :as test-util]))

(deftest token-substitution
  (testing "Make sure tokens are substituted in output at the right place"
    (test-util/harness (fn []
              (core/add-resource :javascript "bing/bong.js")
              (core/add-resource :javascript "bing/bang.js")
              (core/add-resource :javascript "bing/BING!.js" {:location :header})
              (core/add-resource :stylesheet "zing/zong.css")
              (core/add-resource :stylesheet "zing/zang.css")
              (core/add-resource :stylesheet "zing/ZING!.css" {:location :footer})
              (let [template (str "<html><head>\n"
                                  (header-resources)
                                  "</head>\n<body>blah blah\n"
                                  (footer-resources)
                                  "</body></html>")
                    final (core/replace-tokens template)
                    expected "<html><head>
<link rel=\"stylesheet\" href=\"zing/zong.css\">
<link rel=\"stylesheet\" href=\"zing/zang.css\">
<script type=\"text/javascript\" src=\"bing/BING!.js\"></head>
<body>blah blah
<link rel=\"stylesheet\" href=\"zing/ZING!.css\">
<script type=\"text/javascript\" src=\"bing/bong.js\">
<script type=\"text/javascript\" src=\"bing/bang.js\"></body></html>"]
                (is (= final expected)))))))

