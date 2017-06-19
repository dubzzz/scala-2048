/* Taken from https://bootsnipp.com/snippets/featured/minimal-menu */

$(function () {    
    $('.navbar-toggler').on('click', function(event) {
		event.preventDefault();
		$(this).closest('.navbar-minimal').toggleClass('open');
	})
});