<!DOCTYPE html>
<head>
    <meta charset="UTF-8">
    <title>
        Login
    </title>
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
<label>Username:</label>
<input type="text" name="username" />

<label>Password:</label>
<input type="password" name="confirmPassword" />

<button type="submit">Login</button>
</form>
            );
        }

    ReactDOM.render(<LoginForm />, document.getElementById('login-form'));
    </script>
    <p>Don't have an account? <a href="/register">Register here</a>.</p>
</body>
</html>