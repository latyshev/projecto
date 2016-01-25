function myFunction() {
	Papa.parse("results.csv", {
		download: true,
		complete: function(results) {
			console.log("Remote file parsed!", results.data);
			$.each(results.data, function(i, el) {
				var row = $("<tr/>");
				row.append($("<td/>").text(i));
				$.each(el, function(j, cell) {
					if (cell !== "")
						row.append($("<td/>").text(cell));
				});
				$("#results tbody").append(row);
			});
		}
	});
};