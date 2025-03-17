<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>
        Register Account
    </title>
    <script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
    <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body>
<h1>Register Account</h1>
    <div id="register-form"></div>

    <script type="text/babel">
        function RegisterForm(){
            return (
            <form action="/register" method="post">
<label>Username:</label>
<input type="text" name="username" />

<label>Password:</label>
<input type="password" name="confirmPassword" />

<button type="submit">Register</button>
</form>
                );
            }

        ReactDOM.render(<RegisterForm />, document.getElementById('register-form'));
    </script>
    <p>Already have an account? <a href="/login">Login here</a>.</p>
</body>
</html>