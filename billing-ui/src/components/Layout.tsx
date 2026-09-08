import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
    const { username, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    function handleLogout() {
        logout();
        navigate("/login");
    }

    const links = [
        { to: "/dashboard", label: "Units" },
        { to: "/owners", label: "Owners" },
        { to: "/profiles", label: "Profiles" },
        { to: "/invoices", label: "Invoices" },
        { to: "/payments", label: "Payments" },
    ];

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <div className="brand">Ledger</div>
                <div className="brand-sub">Society billing</div>
                <nav className="nav-links">
                    {links.map((l) => (
                        <Link
                            key={l.to}
                            to={l.to}
                            className={`nav-link ${location.pathname === l.to ? "active" : ""}`}
                        >
                            {l.label}
                        </Link>
                    ))}
                </nav>
                <div className="sidebar-footer">
                    <div className="sidebar-user">{username}</div>
                    <button className="btn btn-ghost" onClick={handleLogout} style={{ width: "100%" }}>
                        Log out
                    </button>
                </div>
            </aside>
            <main className="main-panel">
                <Outlet />
            </main>
        </div>
    );
}