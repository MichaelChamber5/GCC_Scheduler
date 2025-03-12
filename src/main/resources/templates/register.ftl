<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>
        Register Account
    </title>
</head>
<body>
<h1>Register Account</h1>
<form action="/register" method="post">
    <div>
        <label for="username">Username:</label>
        <input type="text" id="username" name = "username" required />
    </div>
    <div>
        <label for="confirmedPassword">Confirm Password:</label>
        <input type="password" id="confirmedPassword" name = "confirmPassword" required />
    </div>
    <div>
        <input type="submit" value="Register" />
    </div>
</form>
<p>Already have an account? <a href="/login">Login here</a>.</p>
</body>
</html>