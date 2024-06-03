var chart;
var ohlc, volume, ticker;

function initChart(ohlcJSON, volumeJSON, inputTicker) {
        this.ohlc = ohlcJSON
        this.volume = volumeJSON
        this.ticker = inputTicker

        chart = Highcharts.stockChart('container', {

            chart: {
                      type: 'stockChart',
                      backgroundColor: '#ffffff'
                    },
                    rangeSelector: {
                      selected: 2
                    },
                    title: {
                      text: `${this.ticker} Historical`
                    },
                    subtitle: {
                      text: 'With SMA and Volume by Price technical indicators'
                    },
                    navigator: {
                      enabled: true,
                    },
                    scrollbar: {
                      enabled: true
                    },
                    xAxis: {
                      type: 'datetime'
                    },
                    credits: {
                        enabled: false
                    },
                    exporting: {
                      enabled: false
                  },
                    yAxis: [{
                      startOnTick: false,
                      endOnTick: false,
                      opposite: true,
                      labels: {
                         align: 'right',
                         x: -3
                    },
                      title: {
                        text: 'OHLC'
                      },
                      height: '60%',
                      lineWidth: 2,
                    }, {
                      opposite: true,
                      labels: {
                        align: 'right',
                        x: -3
                      },
                      title: {
                        text: 'Volume'
                      },
                      top: '65%',
                      height: '35%',
                      offset: 0,
                      lineWidth: 2,
                      tickAmount: 4
                    }],
                    tooltip: {
                      split: true
                    },
                    plotOptions: {
                      series: {
                        dataGrouping: {
                          enabled: false
                        }
                      }
                    },
                    series: [{
                      showInLegend: false,
                      type: 'candlestick',
                      name: `${this.ticker}`,
                      id: 'stock',
                      zIndex: 2,
                      data: this.ohlc
                    }, {
                      showInLegend: false,
                      type: 'column',
                      name: 'Volume',
                      id: 'volume',
                      data: this.volume,
                      yAxis: 1
                    }, {
                      type: 'vbp',
                      linkedTo: 'stock',
                      params: {
                        volumeSeriesID: 'volume'
                      },
                      dataLabels: {
                        enabled: false
                      },
                      zoneLines: {
                        enabled: false
                      }
                    }, {
                      type: 'sma',
                      linkedTo: 'stock',
                      zIndex: 1,
                      marker: {
                        enabled: false
                      }
                  }]
        });
}
