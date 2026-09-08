import { createContext , useContext , useState , type ReactNode } from "react";

interface AuthState {
    token : string | null;
    tenantId: string | null;
    username: string | null;
    role: string | null;
    login: (token: string, tenantId: string, username: string , role: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthState | undefined >(undefined);

export function AuthProvider({children}:{children: ReactNode}) {


    const [token, setToken] = useState<string | null>(localStorage.getItem("token"));
    const [tenantId, setTenantId] = useState<string | null>(localStorage.getItem("tenantId"));
    const [username, setUsername] = useState<string | null>(localStorage.getItem("username"));
    const [role, setRole] = useState<string | null>(localStorage.getItem("role"));

    function login(newToken:string, newTenantId:string , newUsername: string, newRole: string){
        localStorage.setItem("token", newToken);
        localStorage.setItem("tenantId", newTenantId);
        localStorage.setItem("username", newUsername);
        localStorage.setItem("role", newRole);

        setToken(newToken);
        setTenantId(newTenantId);
        setRole(newRole);
        setUsername(newUsername);

    }

    function logout(){
        localStorage.clear();
        setToken(null);
        setTenantId(null);
        setUsername(null);
        setRole(null);
    }

    return (
        <AuthContext.Provider value = {{token ,tenantId, username , role , login , logout}}>
            {children}
        </AuthContext.Provider>
    )

}

export function useAuth(){
    const context = useContext(AuthContext);
    if(!context) throw new Error("useAuth must be used within a AuthProvider");
    return context;
}


