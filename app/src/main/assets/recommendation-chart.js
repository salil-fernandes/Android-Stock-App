var chart;
var ticker, trends;
function initChart(data) {
        console.log(data['sb'])
        this.trends = data
        chart = Highcharts.chart('container', {
            chart: {
                    type: 'column',
                    backgroundColor: '#ffffff'
                },
                title: {
                    text: 'Recommendation Trends',
                    align: 'center'
                },
                xAxis: {
                    categories: this.trends['dates']
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: '#Analysis'
                    },
                    stackLabels: {
                        enabled: true,
                        style: {
                            color: '#FFFFFF',
                            textOutline: 'black',
                            strokeWidth: 2.5
                        }
                    }
                },
                legend: {
                    align: 'center',
                    verticalAlign: 'bottom',
                    y: 0,
                    floating: false,
                    backgroundColor:'#f6f6f6',
                    shadow: false
                },
                tooltip: {
                    headerFormat: '<b>{point.x}</b><br/>',
                    pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
                },
                plotOptions: {
                    column: {
                        stacking: 'normal',
                        dataLabels: {
                            enabled: false
                        }
                    }
                },
                credits: {
                      enabled: false
                  },
                series: [{
                  type: 'column',
                  name: 'Strong Buy',
                  data: this.trends['sb'],
                  color: '#1A6334'
              }, {
                type: 'column',
                  name: 'Buy',
                  data: this.trends['b'],
                  color: '#24AF51'
              }, {
                type: 'column',
                  name: 'Hold',
                  data: this.trends['h'],
                  color: '#B07E28'
              },  {
                type: 'column',
                  name: 'Sell',
                  data: this.trends['s'],
                  color: '#F15053'
              }, {
                type: 'column',
                  name: 'Strong Sell',
                  data: this.trends['ss'],
                  color: '#752B2C'
              }]
        });
}
