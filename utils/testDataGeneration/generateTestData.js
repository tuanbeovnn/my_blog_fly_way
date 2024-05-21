const axios = require('axios');
const postData = require('./axiosClient');
const postTemplate = require('./postTemplate.json'); // Import JSON file
const moment = require('moment');

// Define your category IDs for test and production environments
const categoryIdsTest = [
    '7a982f46-138f-4218-848a-54aa231dd13d',
    '39c5b10d-b08e-42a9-a62e-df4065488f4e',
    '733ec3fe-52dd-4eef-95fe-080e709f8da9'
];

const categoryIdsProduction = [
    // Add your production category IDs here
];

// Function to generate random category ID based on environment
const getRandomCategoryId = (environment) => {
    const ids = environment === 'test' ? categoryIdsTest : categoryIdsProduction;
    const randomIndex = Math.floor(Math.random() * ids.length);
    return ids[randomIndex];
};

// Function to calculate the date with offset
const calculateDateWithOffset = (offset) => {
    if (offset.startsWith('{') && offset.endsWith('}')) {
        const daysOffset = parseInt(offset.substring(1, offset.length - 6)); // Extracting the number from the offset string
        if (!isNaN(daysOffset)) {
            return moment().subtract(daysOffset, 'days').toISOString();
        }
    }
    return null; // Return null if offset format is invalid
};

async function main() {
    const args = process.argv.slice(2);
    let action = 'test'; // Default action is set to "test"

    if (args.length >= 3 && args[0] === 'Post') {
        // If the action parameter is provided, use it
        if (args.length === 4) {
            action = args[3].toLowerCase();
        }

        const url = args[1];
        const token = args[2];
        const { posts } = postTemplate; // Access posts array from JSON

        for (const post of posts) {
            // Modify the categoryId field with a random category ID based on the action
            post.categoryId = action === 'test' ? getRandomCategoryId('test') : getRandomCategoryId('production');

            // Set createdDate based on the provided offset, if available
            if (post.createdDate) {
                const calculatedDate = calculateDateWithOffset(post.createdDate);
                if (calculatedDate) {
                    post.createdDate = calculatedDate;
                } else {
                    console.log('Invalid createdDate offset format:', post.createdDate);
                }
            }

            await postData(url, post, token);
        }
    } else {
        console.log('Usage: node generateTestData Post <URL> <Token> [test|production]');
    }
}

main();
