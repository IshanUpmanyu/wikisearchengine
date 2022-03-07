const MAX_RESULTS_PER_PAGE = 10;
var paginationState = {
                        start: 1,
                        current: 1,
                        last: 10
                      };
var searchQuery = "";
$(document).ready(function () {
    $("#home-search-btn").click(function(e) {
        searchQuery = $("#home-search-bar").val();
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
                            updatePagination(searchResponse.total, 1, 1);
                        }
            });
        }
    });

    $("#search-results-search-btn").click(function(e) {
        searchQuery = $("#search-results-page-search-bar").val();
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
                            updatePagination(searchResponse.total, 1, 1);
                        }
            });
        }
    });

    function updatePagination(total, start, current){
        $("#pagination").empty();
        var numOfPages = total / MAX_RESULTS_PER_PAGE + 1;
        let i = start;
        var firstPage = '<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1">Previous</a></li>'
        if(start > 1){
            firstPage = '<li class="page-item"><a class="page-link" href="#" tabindex="-1" page="previous">Previous</a></li>'
        }
        $("#pagination").append(firstPage);
        for(; i <= numOfPages && i < start + 10; i++){
            var pageNumber = '<li class="page-item"><a class = "page-link" href="#" page="'+i+'">'+i+'</a></li>'
            if(i == current){
                pageNumber = '<li class="page-item active "><a class = "page-link" href="#" page="'+i+'">'+i+'</a></li>'
            }
            $("#pagination").append(pageNumber);
        }
        paginationState.last = i-1;
        paginationState.start = start;
        paginationState.current = current;
        var lastPage = null;
        if( numOfPages - start + 1 > 10){
            lastPage = '<li class="page-item"><a class = "page-link" href="#" page="next">next</a></li>'
        }else{
            lastPage = '<li class="page-item disabled"><a class = "page-link" href="#" page='+i+'>next</a></li>'
        }
        $("#pagination").append(lastPage);
    }

    function updateSearchResults(searchResponse){
        const total = searchResponse.total;
        const results = searchResponse.results;
        $("#search-results").empty();
        $("#search-results").append('<p style="padding-left: 20px;">Found '+total+' results. </p>')
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

    $(document).on("click", "a[class=page-link]", function(e) {
        //this == the link that was clicked
        e.preventDefault();
        var page = $(this).attr("page");

        if(page == "next"){
           const searchRequest = {
                query: searchQuery,
                pageNum: paginationState.last + 1,
                resultsPerPage: MAX_RESULTS_PER_PAGE
           };

            $.ajax({
                        type: 'post',
                        url: 'wikisearcher/search',
                        data: JSON.stringify(searchRequest),
                        contentType: "application/json; charset=utf-8",
                        traditional: true,
                        success: function (searchResponse) {
                            updateSearchResults(searchResponse);
                            updatePagination(searchResponse.total, paginationState.last + 1, paginationState.last + 1);
                        }
            });

        }else if(page == "previous"){
            const searchRequest = {
                query: searchQuery,
                pageNum: paginationState.last - 10,
                resultsPerPage: MAX_RESULTS_PER_PAGE
            };

            $.ajax({
                        type: 'post',
                        url: 'wikisearcher/search',
                        data: JSON.stringify(searchRequest),
                        contentType: "application/json; charset=utf-8",
                        traditional: true,
                        success: function (searchResponse) {
                            updateSearchResults(searchResponse);
                            updatePagination(searchResponse.total, paginationState.start - 10, paginationState.start - 10);
                        }
            });
        }else{
            const searchRequest = {
                query: searchQuery,
                pageNum: page - 1,
                resultsPerPage: MAX_RESULTS_PER_PAGE
            };

            $.ajax({
                        type: 'post',
                        url: 'wikisearcher/search',
                        data: JSON.stringify(searchRequest),
                        contentType: "application/json; charset=utf-8",
                        traditional: true,
                        success: function (searchResponse) {
                            updateSearchResults(searchResponse);
                            updatePagination(searchResponse.total, paginationState.start, page);
                        }
            });
        }
    });
});



