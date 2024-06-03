# Stock Trading Android App

This full-stack application simulates live equity trading in a cloud environment. Built with Kotlin on the frontend and Google Cloud, NodeJS on the backend with MongoDB as the primary database, the app is hosted on Google Cloud Platform.

## Features
- **Finnhub Stock APIs**: Fetch data for stock tickers, including price quotes, charts, and news.
- **Autocomplete**: The search bar utilized an autocomplete functionality whenever some characters were entered.
- **Home Section**: Divided into portfolio and favorites sections.
- **Stock Trading**: Buy and sell shares, with transaction results stored in MongoDB.
- **Portfolio Section**: Displays the user’s current portfolio.
- **Favorites Feature**: Allows users to shortlist stocks to monitor.
- **Swipe and Delete** as well as **Drag and Reorder** functions have been implemented.

### Top News
- Shows relevant news articles and updates about the company. Shows relevant news articles and updates about the company.
- Clicking on any news item card opens a modal with clickable links to share the news article as a post on Twitter or Facebook.

<div style="display: flex;">
  <img src="images/2.png" width="800" height="420">
  <img src="images/3.png" width="800" height="420">
</div>

### Charts
- Provides historical stock price variations and technical indicators over different time periods.
- These charts are powered by Highcharts APIs and are interaction-enabled, allowing users to click on any point to see performance metrics such as the stock price at a particular time on a particular day.

### Insights
- Includes insider sentiments, recommendation trends, and historical earnings per share (EPS) surprises.
<img src="images/4.png" width="800" height="420">

## Additional Functionalities
- **Favorites**: Users can keep track of price variations for selected stocks.
- **Portfolio Management**: View and manage current stock holdings.

<div style="display: flex; justify-content: center;">
  <img src="images/5.png" width="800" height="350">
  <img src="images/6.png" width="800" height="420">
</div>
