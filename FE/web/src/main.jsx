import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import CustomerOrderApp from './CustomerOrderApp.jsx'

const path = window.location.pathname;

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {path.startsWith('/customer') ? <CustomerOrderApp /> : <App />}
  </StrictMode>,
)
