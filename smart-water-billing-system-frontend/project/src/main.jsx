import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
        <Toaster
          position="top-right"
          toastOptions={{
            style: {
              background: '#0B1E27',
              color: '#F4F9F9',
              fontFamily: 'Inter, sans-serif',
              fontSize: '13px',
              borderRadius: '10px'
            },
            success: { iconTheme: { primary: '#4AA8D8', secondary: '#F4F9F9' } },
            error: { iconTheme: { primary: '#E15B4F', secondary: '#F4F9F9' } }
          }}
        />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)
