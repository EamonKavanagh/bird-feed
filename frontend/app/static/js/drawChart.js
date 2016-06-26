function drawChart() {

      var chartDiv = document.getElementById('chart_div');

      var data = new google.visualization.DataTable();
      data.addColumn('date', 'Day');
      data.addColumn('number', "Total Sightings");
      data.addColumn('number', "Unique Sightings");

    var classicOptions = {
        title: 'Total and Unique Sightings Over the Last 7 Days',
        width: 1000,
        height: 450,
        series: {
            0: {targetAxisIndex: 0},
            1: {targetAxisIndex: 1}
        },
        vAxes: {
            0: {title: 'Total Sightings'},
            1: {title: 'Unique Sightings'}
        }
    };

    function drawClassicChart() {
        var classicChart = new google.visualization.LineChart(chartDiv);
        classicChart.draw(data, classicOptions);
    }
      
      
    $.get(
        'track/_historical',
        {
            'family': family,
            'name'  : name
        },
        function(dataResponse) {
            var historical = dataResponse.result;
            for (i = 0; i < historical.length; i++) {
                var dataPt = historical[i];
                var date = dataPt[0].split("-");
                date = new Date(date[0], date[1]-1, date[2]);
                data.addRow([date, dataPt[1], dataPt[2]]);
            }
            drawClassicChart();
        }
    );
}