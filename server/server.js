const path = require('path');
const express = require('express')
const cors = require('cors')
const axios = require('axios')
const moment = require('moment-timezone')
const { MongoClient, ServerApiVersion } = require('mongodb')

const app = express()

process.env.TZ = 'America/Los_Angeles';

app.use(express.json())
app.use(cors())

const Finnhub_API_Key = 'cmthun1r01qqtangs3igcmthun1r01qqtangs3j0'
const Polygon_API_Key = 'zIRDWI3M3FpC0Rf4WKZatVjlkpfiheZd'

const mongoPassword = 'Spring2024Assignment3'
const mongoURI = `mongodb+srv://salilfer:` + `${mongoPassword}` + `@cluster0.ofkz8x1.mongodb.net/?retryWrites=true&w=majority`
const client = new MongoClient(mongoURI, {
    serverApi: {
      version: ServerApiVersion.v1,
      strict: true,
      deprecationErrors: true,
    }
  });

app.use(express.static(path.join(__dirname, 'dist/stock-website/browser')));

app.get('/', (request, response) => {
    response.sendFile(path.join(__dirname, 'dist/stock-website/browser/index.html'));
})

// Autocomplete Data
app.get('/autocomplete/:keyword', (request, response) => {
    var key = request.params.keyword.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/search?q=` + key + `&token=` + Finnhub_API_Key).then(
        autoCompleteData => {
            let filteredData = []
            for(let c of autoCompleteData.data.result) {
                if(c.type === "Common Stock" && !c.symbol.includes('.')) {
                    filteredData.push(c)
                }
            }
            response.send(filteredData)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Profile Data
app.get('/company-profile/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/stock/profile2?symbol=` + symbol + `&token=` + Finnhub_API_Key).then(
        profileData => {
            response.send(profileData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Latest Company Stock Data
app.get('/company-latest/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/quote?symbol=` + symbol + `&token=` + Finnhub_API_Key).then(
        latestData => {
            response.send(latestData.data)
            /* var date = new Date(latestData.data.t * 1000);
            console.log(date) */
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Hourly Chart Data
app.get('/company-hourly-chart/:ticker/', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    if(moment().tz('America/Los_Angeles').format('dddd') === 'Monday') {
        var now = moment().tz('America/Los_Angeles').format()
        var current = moment(now).subtract(3, 'days').format().split('T')[0]
        var previous = moment(current).subtract(1, 'days').format().split('T')[0]
    } else if(moment().tz('America/Los_Angeles').format('dddd') === 'Sunday') {
        var now = moment().tz('America/Los_Angeles').format()
        var current = moment(now).subtract(2, 'days').format().split('T')[0]
        var previous = moment(current).subtract(1, 'days').format().split('T')[0]
    } else if(moment().tz('America/Los_Angeles').format('dddd') === 'Saturday') {
        var now = moment().tz('America/Los_Angeles').format()
        var current = moment(now).subtract(1, 'days').format().split('T')[0]
        var previous = moment(current).subtract(1, 'days').format().split('T')[0]
    } else {
        var now = moment().tz('America/Los_Angeles').format()
        var current = now.split('T')[0]
        var previous = moment(current).subtract(1, 'days').format().split('T')[0]
    }
    
    axios.get(`https://api.polygon.io/v2/aggs/ticker/` + symbol + `/range/1/hour/` + previous + `/` + current +  `?adjusted=true&sort=asc&apiKey=` + Polygon_API_Key).then(
        hourlyData => {
            response.send(hourlyData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Peers Data
app.get('/company-peers/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/stock/peers?symbol=` + symbol + `&token=` + Finnhub_API_Key).then(
        peersData => {
            response.send(peersData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company News Data
app.get('/company-news/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()
    var current = new Date()
    var previous = new Date();

    var now = moment().tz('America/Los_Angeles').format()
    var current = now.split('T')[0]
    var previous = moment(current).subtract(30, 'days').format().split('T')[0]

    axios.get(`https://finnhub.io/api/v1/company-news?symbol=` + symbol + `&from=` + previous + `&to=` + current + `&token=` + Finnhub_API_Key).then(
        newsData => {
            response.send(newsData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Main Stock Price and Volume Chart Data
app.get('/company-chart/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()
    
    var now = moment().tz('America/Los_Angeles').format()
    var current = now.split('T')[0]
    var previous = moment(current).subtract(2, 'years').format().split('T')[0]

    axios.get(`https://api.polygon.io/v2/aggs/ticker/` + symbol + `/range/1/day/` + previous + `/` + current +  `?adjusted=true&
    sort=asc&apiKey=` + Polygon_API_Key).then(
        chartData => {
            response.send(chartData.data)
            /* var date = new Date(latestData.data.t * 1000);
            console.log(date) */
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Recommendation Data
app.get('/company-recommendation/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/stock/recommendation?symbol=` + symbol + `&token=` + Finnhub_API_Key).then(
        recommendationData => {
            response.send(recommendationData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Insider Sentiment Data
app.get('/company-insider/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/stock/insider-sentiment?symbol=` + symbol + `&from=2022-01-01&token=` + Finnhub_API_Key).then(
        sentimentData => {
            response.send(sentimentData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// Company Earnings Data
app.get('/company-earnings/:ticker', (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()

    axios.get(`https://finnhub.io/api/v1/stock/earnings?symbol=` + symbol + `&token=` + Finnhub_API_Key).then(
        earningsData => {
            response.send(earningsData.data)
        }
    ).catch(err => {
        response.send({ error : err})
    })
})

// check wallet balance
app.get('/retrieve-balance', async (request, response) => {
    try {
        const database = client.db('stock-website');
        const collection = database.collection('wallet');
        const balance = await collection.find({ name: 'wallet-root' }).toArray();
        response.send(balance)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// update wallet balance
app.put('/update-balance/:newbal', async (request, response) => {
    try {
        var newBalance = request.params.newbal.trim()
        const database = client.db('stock-website');
        const collection = database.collection('wallet');

        let result = await collection.updateOne({ name: 'wallet-root' }, { $set: { balance: Number(newBalance)} });
        result['Updated Balance'] = newBalance
    
    if (result.modifiedCount === 0) {
      return response.status(404).json({ message: "Updated 0 documents." });
    }
    response.status(200).json(result);
  } catch (e) {
    response.status(500).json({ message: e.message });
  }
})

// get stocks in watchlist
app.get('/retrieve-watchlist', async (request, response) => {
    try {
        const database = client.db('stock-website');
        const collection = database.collection('watchlist');
        const watchlist = await collection.find({ name: 'watchlist-root' }).toArray();
        response.send(watchlist[0].watchlist)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// add or remove stocks from watchlist
app.put('/update-watchlist/:type/:ticker', async (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()
    var addOrRemove = request.params.type.trim().toUpperCase()
    try {
        const database = client.db('stock-website');
        const collection = database.collection('watchlist');
        const watchlist = await collection.find({ name: 'watchlist-root' }).toArray();
        let newWatchlist = watchlist[0].watchlist

        if(addOrRemove === 'ADD' && !newWatchlist.includes(symbol)) {
            newWatchlist.push(symbol)
        }

        if(addOrRemove === 'REMOVE' && newWatchlist.includes(symbol)) {
            newWatchlist = newWatchlist.filter((tickers) => {
                return tickers !== symbol
            })
        }

        let result = await collection.updateOne({ name: 'watchlist-root' }, { $set: { watchlist: newWatchlist} });
        result['Updated Watchlist'] = newWatchlist
    
    if (result.modifiedCount === 0) {
      return response.status(404).json({ message: "Updated 0 documents." });
    }
    response.status(200).json(result);
  } catch (e) {
    response.status(500).json({ message: e.message });
  }
})

app.put('/update-watchlist-order', async (req, res) => {
    if (!req.body || !req.body.newWatchlist) {
        return res.status(400).send({ message: "Missing watchlist array in request body" });
    }

    const newWatchlist = req.body.newWatchlist;

    try {
        const database = client.db('stock-website');
        const collection = database.collection('watchlist');
        const result = await collection.updateOne({ name: 'watchlist-root' }, { $set: { watchlist: newWatchlist } });

        if (result.modifiedCount === 0) {
            return res.status(404).send({ message: "No documents were updated" });
        }

        res.status(200).send({ message: "Watchlist order updated successfully", data: newWatchlist });
    } catch (error) {
        console.error('Error updating watchlist:', error);
        res.status(500).send({ message: error.message });
    }
});

// get the stocks in the portfolio
app.get('/retrieve-portfolio', async(request, response) => {
    try {
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const portfolio = await collection.find({ name: 'portfolio-root' }).toArray();
        response.send(portfolio[0].portfolio)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// get the current portfolio data for all stocks in portfolio
app.get('/retrieve-whole-portfolio', async(request, response) => {
    try {
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const portfolio = await collection.find({ name: 'portfolio-stock' }).toArray();
        response.send(portfolio)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// get the current portfolio data for a stock
app.get('/retrieve-stock-portfolio/:ticker', async(request, response) => {
    try {
        var symbol = request.params.ticker.trim().toUpperCase()
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const portfolio = await collection.find({ name: 'portfolio-stock', ticker: symbol }).toArray();
        response.send(portfolio)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// add or remove stocks from portfolio
app.put('/update-portfolio/:type/:ticker', async (request, response) => {
    var symbol = request.params.ticker.trim().toUpperCase()
    var addOrRemove = request.params.type.trim().toUpperCase()
    try {
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const portfolio = await collection.find({ name: 'portfolio-root' }).toArray();
        let newPortfolio = portfolio[0].portfolio

        if(addOrRemove === 'ADD' && !newPortfolio.includes(symbol)) {
            newPortfolio.push(symbol)
        }

        if(addOrRemove === 'REMOVE' && newPortfolio.includes(symbol)) {
            newPortfolio = newPortfolio.filter((tickers) => {
                return tickers !== symbol
            })
        }

        let result = await collection.updateOne({ name: 'portfolio-root' }, { $set: { portfolio: newPortfolio} });
        result['Updated Portfolio'] = newPortfolio
    
    if (result.modifiedCount === 0) {
      return response.status(404).json({ message: "Updated 0 documents." });
    }
    response.status(200).json(result);
  } catch (e) {
    response.status(500).json({ message: e.message });
  }
})

app.put('/update-portfolio-order', async (req, res) => {
    if (!req.body || !req.body.newPortfolio) {
        return res.status(400).send({ message: "Missing portfolio array in request body" });
    }

    const newPortfolio = req.body.newPortfolio;

    try {
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const result = await collection.updateOne({ name: 'portfolio-root' }, { $set: { portfolio: newPortfolio } });

        if (result.modifiedCount === 0) {
            return res.status(404).send({ message: "No documents were updated" });
        }

        res.status(200).send({ message: "Portfolio order updated successfully", data: newPortfolio });
    } catch (error) {
        console.error('Error updating watchlist:', error);
        res.status(500).send({ message: error.message });
    }
});

app.post('/portfolio-buy-firsttime', async(request, response) => {
    try {
        var payload = request.body
        console.log(payload)
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const result = await collection.insertOne(payload);
        response.send(result)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

app.put('/portfolio-buy/:ticker', async(request, response) => {
    try {
        var payload = request.body
        var symbol = request.params.ticker.trim().toUpperCase()
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const result = await collection.updateOne({ name: 'portfolio-stock', ticker: symbol }, { $set: { quantity: payload.quantity, costpershare: payload.costpershare, change: payload.change, current: payload.current, market: payload.market, total: payload.total}});
        response.send(result)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

// selling all shares of a stock
app.delete('/portfolio-sell-whole-stock/:ticker', async (request, response) => {
    try {
      var symbol = request.params.ticker.trim().toUpperCase();
      const database = client.db('stock-website');
      const collection = database.collection('portfolio');
  
      const result = await collection.deleteOne({ name: 'portfolio-stock', ticker: symbol });
  
      if (result.deletedCount === 0) {
        return response.status(404).json({ message: "Stock not found or already sold." });
      }
      response.json(result);
    } catch (e) {
      response.status(500).json({ message: e.message });
    }
  });

// selling only a few shares
app.put('/portfolio-sell/:ticker', async(request, response) => {
    try {
        var payload = request.body
        var symbol = request.params.ticker.trim().toUpperCase()
        const database = client.db('stock-website');
        const collection = database.collection('portfolio');
        const result = await collection.updateOne({ name: 'portfolio-stock', ticker: symbol }, { $set: { quantity: payload.quantity, costpershare: payload.costpershare, change: payload.change, current: payload.current, market: payload.market, total: payload.total}});
        response.send(result)
      } catch (e) {
        response.status(500).json({ message: e.message });
      }
})

client.connect().then((client) => {
    dbConnection = client.db('stock-website');
    console.log('Connected to MongoDB');

    const port = process.env.port || 8080;
    app.listen(port, () => {
        console.log(`NodeJS Stock server running on port ${port}`);
    });
}).catch((err) => {
    console.log('Error connecting to MongoDB', err);
})