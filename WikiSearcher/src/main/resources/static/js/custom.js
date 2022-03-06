$(document).ready(function () {
    $("#home-search-btn").click(function(e) {
       $("#home-search-btn").closest("#home-page").toggleClass("hidden");
       $("#search-results-page").toggleClass("hidden");
        //$("#search-results-page").show();
    });
});