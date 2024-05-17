const axios = require('axios');

async function postData(data, token) {
    try {
        const response = await axios.post('http://localhost:8080/api/v1/posts', data, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('Data posted successfully:', response.data);
    } catch (error) {
        console.error('Error posting data:', error.response ? error.response.data : error.message);
    }
}

module.exports = postData;
