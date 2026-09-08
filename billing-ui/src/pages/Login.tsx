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
        } catch (err) {
            setError("Invalid username or password");
        }
    }

    return (
        <div style={{ maxWidth: 400, margin: "100px auto" }}>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Username</label>
                    <input value={username} onChange={(e) => setUsername(e.target.value)} />
                </div>
                <div>
                    <label>Password</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </div>
                {error && <p style={{ color: "red" }}>{error}</p>}
                <button type="submit">Log In</button>
            </form>
        </div>
    );
}

