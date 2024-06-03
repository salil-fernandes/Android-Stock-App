var chart;
var ticker, categories, actuals, estimates;
function initChart(category, actual, estimate) {
        this.categories = category
        this.actuals = actual
        this.estimates = estimate
        chart = Highcharts.chart('container', {
            chart: {
                    type: 'spline',
                    backgroundColor: '#ffffff'
                },
                title: {
                    text: 'Historical EPS Surprises'
                },
                xAxis: {
                    categories: this.categories,
                    accessibility: {
                        description: 'Months of the year'
                    },
                    rotation: -45,
                    align: 'right',
                    useHTML: true
                },
                yAxis: {
                    title: {
                        text: 'Quarterly EPS'
                    },
                },
                tooltip: {
                    shared: true
                },
                credits: {
                      enabled: false
                  },
                series: [{
                  type: 'spline',
                    name: 'Actual',
                    marker: {
                    		radius: 4,
                        symbol: 'circle'
                    },
                    data: this.actuals,
                    label: {
                        enabled: true
                    }

                }, {
                  type: 'spline',
                    name: 'Estimate',
                    marker: {
                    		radius: 4,
                        symbol: 'diamond'
                    },
                    data: this.estimates,
                    label: {
                        enabled: true
                    }
                }]
        });
}
