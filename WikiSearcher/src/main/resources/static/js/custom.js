const MAX_RESULTS_PER_PAGE = 10;
$(document).ready(function () {
    $("#home-search-btn").click(function(e) {
        const searchQuery = $("#home-search-bar").val();
        if(searchQuery.length !== 0){
            $("#home-search-btn").closest("#home-page").toggleClass("hidden");
            $("#search-results-page").toggleClass("hidden");
            const searchRequest = {
                query: searchQuery,
                pageNum: 0,
                resultsPerPage: MAX_RESULTS_PER_PAGE
            };

            $("#search-results-page-search-bar").val(searchQuery);

            $.ajax({
                        type: 'post',
                        url: 'wikisearcher/search',
                        data: JSON.stringify(searchRequest),
                        contentType: "application/json; charset=utf-8",
                        traditional: true,
                        success: function (searchResponse) {
                            updateSearchResults(searchResponse);
                            updatePagination(searchResponse.total);
                        }
            });
        }
    });

    $("#search-results-search-btn").click(function(e) {
        const searchQuery = $("#search-results-page-search-bar").val();
        if(searchQuery.length !== 0){
            const searchRequest = {
                query: searchQuery,
                pageNum: 0,
                resultsPerPage: MAX_RESULTS_PER_PAGE
            };

            $("#search-results-page-search-bar").val(searchQuery);

            $.ajax({
                        type: 'post',
                        url: 'wikisearcher/search',
                        data: JSON.stringify(searchRequest),
                        contentType: "application/json; charset=utf-8",
                        traditional: true,
                        success: function (searchResponse) {
                            updateSearchResults(searchResponse);
                            updatePagination(searchResponse.total);
                        }
            });
        }
    });

    function updatePagination(total){
        $("#pagination").empty();
        var numOfPages = total / MAX_RESULTS_PER_PAGE + 1;
        let i = 1;
        for(; i <= numOfPages; i++){
            const pageNumber = '<div class="p-2"><a class = "pagination-link" href="#" page='+i+'>'+i+'</a></div>'
            $("#pagination").append(pageNumber);
            if( i >= 10){
                break;
            }
        }
        if( numOfPages > 10){
            const next = '<div class="p-2 "><a class = "pagination-link" href="#" page='+i+'>next</a></div>'
            $("#pagination").append(next);
        }
    }

    function updateSearchResults(searchResponse){
        const total = searchResponse.total;
        const results = searchResponse.results;
        $("#search-results").empty();
        results.forEach(addSearchResult)
    }

    function addSearchResult(searchResult){

        const searchResultHTML =
        '<div class="search-result">' +
        '    <a href="'+searchResult.url+'">'+ searchResult.title + ' - Wikipedia</a> </br>' +
        ' <p>' + searchResult.description + '</p>' +
        '</div>'

        console.log(searchResultHTML);
         $("#search-results").append(searchResultHTML);

    }

    $(document).on("click", "a[class=pagination-link]", function(e) {
        //this == the link that was clicked
        e.preventDefault();
        var page = $(this).attr("page");
        alert("You're trying to go to page" + page);
    });
});



