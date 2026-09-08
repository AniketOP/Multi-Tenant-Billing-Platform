import { Link, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
    const { username, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/login");
    }

    return (
        <div>
            <nav style={{ display: "flex", gap: 16, padding: 16, borderBottom: "1px solid #333" }}>
                <Link to="/dashboard">Units</Link>
                <Link to="/owners">Owners</Link>
                <Link to="/profiles">Profiles</Link>
                <Link to="/invoices">Invoices</Link>
                <Link to="/payments">Payments</Link>
                <span style={{ marginLeft: "auto" }}>{username}</span>
                <button onClick={handleLogout}>Log Out</button>
            </nav>
            <div style={{ maxWidth: 900, margin: "20px auto", padding: "0 16px" }}>
                <Outlet />
            </div>
        </div>
    );
}