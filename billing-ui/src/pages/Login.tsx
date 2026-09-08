import { useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const { login } = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        try {
            const response = await apiClient.post("/auth/login", { username, password });
            const { token, tenantId, username: respUsername, role } = response.data;
            login(token, tenantId, respUsername, role);
            navigate("/dashboard");
        } catch {
            setError("Invalid username or password");
        }
    }

    return (
        <div className="login-shell">
            <div className="login-hero">
                <div className="ledger-lines">
                    {Array.from({ length: 14 }).map((_, i) => (
                        <div key={i} style={{ top: `${(i + 1) * 44}px` }} />
                    ))}
                </div>
                <h1>The record of<br />every unit,<br />settled.</h1>
                <p>
                    One ledger for every owner, invoice, and payment across your society —
                    kept in balance automatically.
                </p>
            </div>
            <div className="login-form-side">
                <form className="login-card" onSubmit={handleSubmit}>
                    <h2>Sign in</h2>
                    <label>Username</label>
                    <input value={username} onChange={(e) => setUsername(e.target.value)} />
                    <label>Password</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                    {error && <p className="error-text" style={{ marginTop: 16 }}>{error}</p>}
                    <button type="submit">Sign in</button>
                </form>
            </div>
        </div>
    );
}
