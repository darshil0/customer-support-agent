# FinAgent Pro

FinAgent Pro is a sophisticated financial analysis dashboard that provides daily insights into the S&P 500 stock market performance. Powered by **Google Gemini 2.5 Flash**, it delivers real-time, AI-generated reports grounded in up-to-date web sources, alongside visual breakdowns of sector performance.

## 🚀 Features

*   **AI-Generated Daily Reports**: Detailed breakdown of market snapshots, key drivers, top movers, and future outlooks.
*   **Real-Time Grounding**: Uses the Google Search tool to fetch and cite the latest financial news and data sources.
*   **Visual Analytics**: Interactive S&P 500 Sector Allocation pie chart using `recharts` with drill-down details.
*   **Interactive Watchlist**: 
    *   Real-time stock tracking with **5-day sparkline trends**.
    *   **Stock Comparison**: Compare the performance of up to 3 stocks side-by-side with a dedicated percentage trend chart.
    *   **Client-side sorting** by Symbol, Price, or Percentage Change.
    *   **Filtering** capabilities to quickly find stocks.
*   **Modern UI**: Fully responsive, accessible, dark-themed interface built with Tailwind CSS.
*   **Robust Error Handling**: Graceful fallbacks for API limits or network issues.

## 🛠️ Tech Stack

*   **Frontend**: React 18, TypeScript, Vite (implied)
*   **Styling**: Tailwind CSS
*   **AI/ML**: Google GenAI SDK (`@google/genai`), Model: `gemini-2.5-flash`
*   **Visualization**: Recharts (Pie/Line Charts), SVG (Sparklines)
*   **Icons**: FontAwesome

## 🔧 Setup & Configuration

### Prerequisites
*   Node.js installed.
*   A Google Gemini API Key.

### Environment Variables
The application requires a valid API key accessible via `process.env.API_KEY`.

```bash
# Example .env
API_KEY=your_gemini_api_key_here
```

### Installation

1.  Clone the repository.
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start
    ```

## 🧪 Testing

The application is instrumented with `data-testid` attributes to support robust End-to-End (E2E) and Regression testing.

### Key Test Selectors

| Component | Test ID | Description |
| :--- | :--- | :--- |
| **Hero Section** | `generate-report-btn` | The main CTA to start the analysis. |
| **Loading** | `loading-state` | Visible while the AI is generating content. |
| **Error** | `error-state` | Visible if the API call fails. |
| **Report Area** | `report-container` | Main container for the generated report. |
| **Report Content** | `report-markdown-body` | The rendered Markdown text. |
| **Chart** | `market-chart-container` | The container for the Sector Pie Chart. |
| **Refresh** | `refresh-analysis-btn` | Button to regenerate data. |
| **Sources** | `grounding-sources` | Container for cited web links. |

## 📂 Project Structure

*   `src/index.tsx`: Entry point.
*   `src/App.tsx`: Main application controller and layout.
*   `src/components/`: Reusable UI components (`MarketChart`, `ReportView`, `Header`, `TickerTracker` etc.).
*   `src/services/geminiService.ts`: Integration with Google GenAI SDK.
*   `src/types.ts`: TypeScript interfaces for robust type checking.

## 📝 License

This project is open source and available under the MIT License.