import CreateLinkForm from "./components/CreateLinkForm";

function App() {
  return (
    <main className="app">
      <h1>URL Shortener</h1>
      <p>Manage your short links in one place.</p>

      <CreateLinkForm />
    </main>
  );
}

export default App;
