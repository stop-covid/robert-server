import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch} from 'react-router-dom';
import Header from './components/header';
import MainContainer from './components/main-container';
import Home from './pages/home';
import About from './pages/about';
/* The following line can be included in your src/index.js or App.js file */
import './styles/app.sass';
import {Nav, Navbar} from 'react-bootstrap';
import ConfigurationDetails from "./pages/configuration-details";

export default function App() {
  return (
      <div style={{height: '100vh', width: '100vw'}}>
        <Router>
          <Header>
            <Navbar
                variant="dark"
                bg="dark"
                expand="lg"
            >
              <Navbar.Brand href="/">Administration fonctionnelle</Navbar.Brand>

              <Navbar.Toggle
                  aria-controls="basic-navbar-nav"
              />

              <Navbar.Collapse id="basic-navbar-nav">
                <Nav className="mr-auto">
                  <Nav.Link>
                    <Link to="/">Home</Link>
                  </Nav.Link>

                  <Nav.Link>
                    <Link to="/configuration">Configuration</Link>
                  </Nav.Link>

                  <Nav.Link>
                    <Link to="/about">About</Link>
                  </Nav.Link>
                </Nav>
              </Navbar.Collapse>

            </Navbar>
          </Header>

          <MainContainer>
            <Switch>
              <Route path="/about">
                <About/>
              </Route>

              <Route path="/configuration">
                <ConfigurationDetails/>
              </Route>


              <Route path="/">
                <Home />
              </Route>
            </Switch>
          </MainContainer>

        </Router>
      </div>

  );
}
