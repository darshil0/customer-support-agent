# FinAgent Pro

FinAgent Pro is a sophisticated financial analysis dashboard that provides daily insights into the S&P 500 stock market performance. Powered by **Google Gemini 2.0 Flash**, it delivers real-time, AI-generated reports grounded in up-to-date web sources, alongside visual breakdowns of sector performance.

## 🚀 Features

*   **AI-Generated Daily Reports**: Detailed breakdown of market snapshots, key drivers, top movers, and future outlooks.
*   **Real-Time Grounding**: Uses Google Search to fetch and cite the latest financial news and data sources.
*   **Visual Analytics**: Interactive S&P 500 Sector Allocation pie chart using `recharts` with drill-down details.
*   **Interactive Watchlist**:
    *   Real-time stock tracking with **5-day sparkline trends**.
    *   **Stock Comparison**: Compare the performance of up to 3 stocks side-by-side with a dedicated percentage trend chart.
    *   **Client-side sorting** by Symbol, Price, or Percentage Change.
    *   **Filtering** capabilities to quickly find stocks.
*   **Modern UI**: Fully responsive, accessible, dark-themed interface built with Tailwind CSS.
*   **Robust Error Handling**: Graceful fallbacks for API limits or network issues.
*   **API Key Fallback**: The application will return a mock report if the API key is not configured, allowing for development and testing without a valid key.

## 🛠️ Tech Stack

*   **Frontend**: React 18, TypeScript, Vite
*   **Styling**: Tailwind CSS
*   **AI/ML**: Google GenAI SDK (`@google/generative-ai`), Model: `gemini-2.0-flash-exp`
*   **Visualization**: Recharts (Pie/Line Charts), SVG (Sparklines)
*   **Markdown**: react-markdown
*   **Testing**: Vitest, React Testing Library
*   **Linting**: ESLint (Flat Config)

## 🔧 Setup & Configuration

### Prerequisites

*   Node.js (v18 or higher)
*   npm or yarn
*   A Google Gemini API Key ([Get one here](https://makersuite.google.com/app/apikey)) (Optional for development)

### Quick Start

1.  **Clone the repository**
    ```bash
    git clone https://github.com/darshil0/FinAgent-Pro.git
    cd FinAgent-Pro
    ```

2.  **Install dependencies**
    ```bash
    npm install
    ```

3.  **Configure environment variables**
    ```bash
    cp .env.example .env
    ```

    Edit `.env` and add your Google Gemini API key:
    ```env
    VITE_API_KEY=your_gemini_api_key_here
    ```
    *Note: If you do not provide an API key, the application will run in a mock data mode.*

4.  **Start the development server**
    ```bash
    npm run dev
    ```

5.  **Open your browser**
    Navigate to `http://localhost:3000`

### Build for Production

```bash
npm run build
npm run preview
```

## 📂 Project Structure

```
FinAgent-Pro/
├── components/
│   ├── ErrorBoundary.tsx
│   ├── Header.tsx
│   ├── Hero.tsx
│   ├── LoadingState.tsx
│   ├── ErrorState.tsx
│   ├── ReportView.tsx
│   ├── MarketChart.tsx
│   ├── TickerTracker.tsx
│   ├── Sparkline.tsx
│   └── StockComparison.tsx
├── services/
│   └── geminiService.ts
├── src/
│   └── setupTests.ts
├── App.tsx
├── index.tsx
├── vite.config.ts
├── vitest.config.ts
├── tsconfig.json
├── eslint.config.js
└── .env.example
```

## 🐛 Troubleshooting

### API Key Issues

**Error**: "Invalid API key" or "VITE_API_KEY is not defined"

**Solutions**:
*   Verify your API key at [Google AI Studio](https://makersuite.google.com/app/apikey)
*   Make sure your `.env` file has `VITE_API_KEY=your_actual_key` (not wrapped in quotes)
*   Restart the dev server after changing `.env`: Stop the server (Ctrl+C) and run `npm run dev` again
*   Check that `.env` is in the root directory (same level as `package.json`)

### Rate Limit Errors

**Error**: "Rate limit exceeded"

**Solutions**:
*   Free tier allows 60 requests per minute
*   Wait 60 seconds before trying again
*   Consider upgrading your API plan for higher limits
*   The app automatically retries with exponential backoff

### Build Errors

**Error**: TypeScript or build compilation errors

**Solutions**:
```bash
# Clean install
rm -rf node_modules package-lock.json
npm install

# Clear cache and rebuild
npm run build
```

### Chart Not Rendering

**Solutions**:
*   Check browser console for errors
*   Ensure all dependencies are installed: `npm install recharts react-markdown`
*   Clear browser cache and reload
*   Try a different browser

### Environment Variables Not Loading

**Solutions**:
*   Vite requires `VITE_` prefix for all env variables
*   Use `import.meta.env.VITE_API_KEY` not `process.env.API_KEY`
*   Restart dev server after changing `.env`

## 🧪 Testing

The application uses **Vitest** for testing. The test suite includes unit and integration tests.

### Running Tests

```bash
# Run all tests
npm test

# Run tests with the UI
npm run test:ui

# Generate a coverage report
npm run test:coverage
```

### Key Test Selectors

| Component           | Test ID                | Description                       |
| ------------------- | ---------------------- | --------------------------------- |
| **Hero Section**    | `generate-report-btn`  | Main CTA to start analysis        |
| **Loading**         | `loading-state`        | Loading indicator                 |
| **Error**           | `error-state`          | Error display                     |
| **Report**          | `report-container`     | Report container                  |
| **Report Content**  | `report-markdown-body` | Rendered markdown                 |
| **Chart**           | `market-chart-container`| Sector pie chart                 |
| **Refresh**         | `refresh-analysis-btn` | Refresh button                    |
| **Sources**         | `grounding-sources`    | Cited sources                     |
| **Watchlist**       | `ticker-tracker`       | Stock watchlist                   |

## 🚀 Deployment

### Deploy to Vercel

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel
```

Add your `VITE_API_KEY` in Vercel project settings under Environment Variables.

### Deploy to Netlify

```bash
# Build the project
npm run build

# Deploy the dist folder
```

Add your `VITE_API_KEY` in Netlify site settings under Environment Variables.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1.  Fork the repository
2.  Create a feature branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

*   Powered by [Google Gemini 2.0 Flash](https://ai.google.dev/)
*   Built with [React](https://react.dev/) and [TypeScript](https://www.typescriptlang.org/)
*   Styled with [Tailwind CSS](https://tailwindcss.com/)
*   Charts by [Recharts](https://recharts.org/)

## 📧 Support

If you encounter any issues or have questions:

1.  Check the [Troubleshooting](#-troubleshooting) section
2.  Search existing [GitHub Issues](https://github.com/darshil0/FinAgent-Pro/issues)
3.  Create a new issue with detailed information

## ⚠️ Disclaimer

This application generates AI-powered analysis and should not be considered financial advice. Always conduct your own research and consult with a qualified financial advisor before making investment decisions.

---

Made with ❤️ by [darshil0](https://github.com/darshil0)
