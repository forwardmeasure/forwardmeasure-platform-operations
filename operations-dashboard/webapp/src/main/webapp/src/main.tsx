import "@fontsource-variable/inter";
import React from "react";
import ReactDOM from "react-dom/client";
import { App } from "./App";
import { initialize } from "./runtime";
import "./styles.css";
import { dashboardTitle } from "./branding";

document.title = dashboardTitle();

const root = ReactDOM.createRoot(document.getElementById("root")!);
root.render(<div className="boot">Opening {dashboardTitle()}…</div>);

initialize()
  .then(identity => root.render(<React.StrictMode><App identity={identity}/></React.StrictMode>))
  .catch(failure => root.render(<main className="fatal" role="alert"><p>{dashboardTitle()}</p><h1>Unable To Start This View</h1><pre>{failure instanceof Error ? failure.message : String(failure)}</pre></main>));
