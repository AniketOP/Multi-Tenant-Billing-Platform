import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Owners from "./pages/Owners";
import Profiles from "./pages/Profiles";
import Invoices from "./pages/Invoices";
import Payments from "./pages/Payments";
import ProtectedRoute from "./components/ProtectedRoutes.tsx";
import Layout from "./components/Layout";

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route
                        element={
                            <ProtectedRoute>
                                <Layout />
                            </ProtectedRoute>
                        }
                    >
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/owners" element={<Owners />} />
                        <Route path="/profiles" element={<Profiles />} />
                        <Route path="/invoices" element={<Invoices />} />
                        <Route path="/payments" element={<Payments />} />
                    </Route>
                    <Route path="/" element={<Navigate to="/login" />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;