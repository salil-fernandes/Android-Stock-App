var chart;
var ticker, hourlyData, chartColor;
function initChart(data, color, inputTicker) {
        this.chartColor = color
        this.hourlyData = data
        this.ticker = inputTicker
        chart = Highcharts.chart('container', {
            chart: {
                type: 'line',
                backgroundColor: '#ffffff',
            },
            title: {
                text: `${this.ticker} Price Variation`,
            },
            xAxis: {
                type: 'datetime',
                accessibility: {
                    enabled: false
                }
            },
            yAxis: {
                opposite: true,
                title: {
                    text: ''
                },
                labels: {
                    x: -15,
                    y: -5
                },
                tickAmount: 5
            },
            legend: {
                enabled: false
            },
            scrollbar: {
                enabled: true
            },
            credits: {
                enabled: true
            },
            rangeSelector: {
                enabled: false
            },
            navigator: {
                enabled: false
            },
            exporting: {
                    enabled: false
            },
            tooltip: {
                shared: true,
                useHTML: true,
                crosshairs: true,
                pointFormat: '{point.x:%e. %b}: {point.y:.2f} m',
                formatter: function () {
                    return ticker + ': <b>' +  this.y + '</b>' + '<br/>' + Highcharts.dateFormat('%A, %b %e, %H:%M', this.x);
                }
            },
            series: [{
                type: 'line',
                name: '',
                data: this.hourlyData,
                color: this.chartColor,
                marker: {
                    enabled: false,
                    states: {
                        hover: {
                            enabled: true,
                            radius: 5
                        }
                    }
                }
            }]
        });
}

function updateChart(jsonData, color, ticker) {
    if (chart && chart.series[0]) {
         chart.series[0].setData(jsonData);
         chart.series[0].update({
                     color: color // Change color of the line based on the input parameter
         });
         chart.setTitle({ text: ticker + ' Hourly Price Variation' });
         chart.series[0].name = ticker
         chart.redraw()
    }
}
