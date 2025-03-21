<!DOCTYPE html>
<head>
    <meta charset="UTF-8">
    <title>
        Login
    </title>
    <link rel="stylesheet" href="/styles/loginRegister.css">
    <script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
    <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body>
<h1>Login</h1>
    <div id="login-form"></div>

    <script type="text/babel">
        function LoginForm() {
            return (
                <form action="/login" method="post">
                    <div>
                        <label htmlFor="username">Username: </label>
                        <input type="text" id="username" name = "username" required />
                    </div>
                    <div>
                        <label htmlFor="confirmedPassword">Confirm Password: </label>
                        <input type="password" id="confirmedPassword" name = "confirmPassword" required />
                    </div>
                    <div>
                    <input type="submit" value="Login" />
                    </div>
                </form>
            );
        }

    ReactDOM.render(<LoginForm />, document.getElementById('login-form'));
    </script>
    <p>Don't have an account? <a href="/register">Register here</a>.</p>
</body>
</html>
<!-- this comment was strictly added to I can commit -->
