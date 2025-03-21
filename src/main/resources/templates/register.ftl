<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register Account</title>
    <link rel="stylesheet" href="/styles/loginRegister.css">
    <script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
    <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body>
<h1>Register Account</h1>
<div id="register-form"></div>

<script type="text/babel">
    function RegisterForm(){
        const [password, setPassword] = React.useState('');
        const [confirmPassword, setConfirmPassword] = React.useState('');

        const handleSubmit = (e) => {
            if (password !== confirmPassword) {
                alert("Passwords do not match!");
                e.preventDefault();
            }
        };

        return (
            <form action="/register" method="post" onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username: </label>
                    <input type="text" id="username" name="username" required />
                </div>
                <div>
                    <label htmlFor="password">Password: </label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <div>
                    <label htmlFor="confirmPassword">Confirm Password: </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        required
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                </div>
                <div>
                    <input type="submit" value="Register" />
                </div>
            </form>
        );
    }

    ReactDOM.render(<RegisterForm />, document.getElementById('register-form'));
</script>
<p>Already have an account? <a href="/login">Login here</a>.</p>
</body>
</html>
<!-- this comment was strictly added to I can commit -->
