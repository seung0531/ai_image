import { Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MyImages from "./pages/MyImages";
import ImageDetail from "./pages/ImageDetail";
import PublicGallery from "./pages/PublicGallery";
import PublicImageDetail from "./pages/PublicImageDetail";
import "./App.css";

function App() {
    return (
        <>
            <Navbar />

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
                <Route path="/my-images" element={<MyImages />} />
                <Route path="/images/:imageId" element={<ImageDetail />} />
                <Route path="/gallery" element={<PublicGallery />} />
                <Route path="/gallery/:imageId" element={<PublicImageDetail />} />
            </Routes>
        </>
    );
}

export default App;